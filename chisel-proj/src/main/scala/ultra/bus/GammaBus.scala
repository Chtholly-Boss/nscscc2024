package ultra.bus
import chisel3._
import chisel3.util._
import UltraBusPorts._
import UltraBusUtils._
import UltraBusParams._
import ultra.bus.uart.{async_receiver, async_transmitter}
import sram.SramUtils._
import ultra.bus.GammaBus.{byteSelInWords, pc2BaseAddr, va2deviceAddr}
class GammaBus extends Module {
  val io = IO(new UltraBusIo)
  io.iChannel.out := initInstRspns
  io.dChannel.out := initDataRspns
  /**
   * Sram Control Outputs
   * Synchronous to be friendly
   */
  val bRamOutReg = RegInit(initSramReq)
  val eRamOutReg = RegInit(initSramReq)
  io.baseRam.out := bRamOutReg
  io.extRam.out := eRamOutReg

  /**
   * Uart:
   * transmitter: synchronous start and data
   * receiver: synchronous clear
   */
  val uTransmitter = Module(new async_transmitter(cpuFrequency))
  val uReceiver = Module(new async_receiver(cpuFrequency))
  val uTdata = RegInit(0.U(8.W))
  val uTstart = RegInit(false.B)
  val uRclear = RegInit(false.B)
  // Transmitter Connection
  uTransmitter.io.clk := clock
  io.uart.txd := uTransmitter.io.TxD
  uTransmitter.io.TxD_start := uTstart
  uTransmitter.io.TxD_data := uTdata
  // Receiver Connection
  uReceiver.io.clk := clock
  uReceiver.io.RxD_clear := uRclear
  uReceiver.io.RxD := io.uart.rxd
  // Uart Status
  val uStatus = WireDefault(uReceiver.io.RxD_data_ready ## ~uTransmitter.io.TxD_busy)

  /**
   *  State Members:
   *  - irStat: instruction load state
   *  - drStat: data load state
   *  - dwStat: data write state
   *  - bRamStat: BaseSram load/store state
   *  - eRamStat: ExtSram load/store state
   *  - uStat: Uart Check/Load/Store state
   */
  object WS extends ChiselEnum {
    val
      IDLE,
      WAIT
    = Value
  }
  val irStat = RegInit(WS.IDLE)
  val drStat = RegInit(WS.IDLE)
  val dwStat = RegInit(WS.IDLE)

  io.iChannel.out.rrdy := irStat === WS.IDLE
  io.dChannel.out.rrdy := drStat === WS.IDLE
  io.dChannel.out.wrdy := dwStat === WS.IDLE

  object BS extends ChiselEnum {
    val
      IDLE,
      INST_LOAD,
      DATA_LOAD,
      DATA_STORE
    = Value
  }
  val bRamStat = RegInit(BS.IDLE)

  object ES extends ChiselEnum {
    val
      IDLE,
      LOAD,
      STORE
    = Value
  }
  val eRamStat = RegInit(ES.IDLE)

  val isEramBusy = WireDefault(!(eRamStat === ES.IDLE))

  object US extends ChiselEnum {
    val
      IDLE,
      LOAD,
      STORE
    = Value
  }
  val uStat = RegInit(US.IDLE)
  /**
   * Buffers:
   * - irBuf: instruction load request buffer
   * - drBuf: data load request buffer
   * - dwBuf: data store request buffer
   */
  val irBuf = RegInit(initInstReq)
  val drBuf = RegInit(initDataReq)
  val dwBuf = RegInit(initDataReq)

  /**
   * Counters:
   * - iWordCounter
   * - bRamCounter: Counting cycles
   * - eRamCounter: Counting cycles
   */
  val iWordCounter = Counter(iWords + 1)
  val bRamCounter = Counter(memCycles)
  val eRamCounter = Counter(memCycles)

  /**
   * Instruction Channel Side Logic:
   */
  val isInst2BaseLoad = RegInit(false.B)
  val inst2BaseLoad = WireDefault(isInst2BaseLoad)
  switch(irStat){
    is(WS.IDLE){
      when(io.iChannel.in.rreq){
        irStat := WS.WAIT
        irBuf := io.iChannel.in
        inst2BaseLoad := true.B
        isInst2BaseLoad := true.B
      }
    }
    is(WS.WAIT){
      // Determined by (bRamStat === InstLoad)
    }
  }

  /**
   * Data Channel Side Logic
   */
  val isData2ExtLoadBlock = RegInit(false.B)
  val isData2ExtStoreBlock = RegInit(false.B)
  val data2ExtLoadBlock = WireDefault(isData2ExtLoadBlock)
  val data2ExtStoreBlock = WireDefault(isData2ExtStoreBlock)

  val isData2BaseLoad = RegInit(false.B)
  val isData2BaseStore = RegInit(false.B)
  val data2BaseLoad = WireDefault(isData2BaseLoad)
  val data2BaseStore = WireDefault(isData2BaseStore)
  switch(drStat){
    is(WS.IDLE){
      when(io.dChannel.in.rreq){
        drStat := WS.WAIT
        drBuf := io.dChannel.in
        when(va2deviceAddr(io.dChannel.in.addr) === extSramAddr){
          when(isEramBusy){
            isData2ExtLoadBlock := true.B
            data2ExtLoadBlock := true.B
          }.otherwise{
            eRamCounter.reset()
            eRamOutReg := sramReadWord(io.dChannel.in.addr(21,2))
            eRamStat := ES.LOAD
          }
        }
        when(va2deviceAddr(io.dChannel.in.addr) === uartAddr){
          uStat := US.LOAD
        }
        when(va2deviceAddr(io.dChannel.in.addr) === baseSramAddr){
          isData2BaseLoad := true.B
          data2BaseLoad := true.B
        }
      }
    }
    is(WS.WAIT){
      // Determined by bRamStat and eRamStat
    }
  }
  switch(dwStat){
    is(WS.IDLE){
      when(io.dChannel.in.wreq){
        dwStat := WS.WAIT
        dwBuf := io.dChannel.in
        when(va2deviceAddr(io.dChannel.in.addr) === extSramAddr){
          when(isEramBusy){
            isData2ExtStoreBlock := true.B
            data2ExtStoreBlock := true.B
          }.otherwise{
            eRamCounter.reset()
            eRamOutReg := sramWrite(
              io.dChannel.in.addr(21,2),
              io.dChannel.in.wdata,
              io.dChannel.in.byteSelN
            )
            eRamStat := ES.STORE
          }
        }
        when(va2deviceAddr(io.dChannel.in.addr) === uartAddr){
          uStat := US.STORE
        }
        when(va2deviceAddr(io.dChannel.in.addr) === baseSramAddr){
          isData2BaseStore := true.B
          data2BaseStore := true.B
        }
      }
    }
    is(WS.WAIT){
      // Determined by bRamStat and eRamStat
    }
  }
  /**
   * BaseSram Side Logic
   */
  val irData = RegInit(0.U(iBandWidth.W))
  switch(bRamStat){
    is(BS.IDLE){
      when(data2BaseStore){
        isData2BaseStore := false.B
        bRamCounter.reset()
        bRamStat := BS.DATA_STORE
        when(io.dChannel.in.wreq){
          bRamOutReg := sramWrite(
            io.dChannel.in.addr(21,2),
            io.dChannel.in.wdata,
            io.dChannel.in.byteSelN
          )
        }.otherwise{
          bRamOutReg := sramWrite(dwBuf.addr(21,2),dwBuf.wdata,dwBuf.byteSelN)
        }
      }.elsewhen(data2BaseLoad){
        isData2BaseLoad := false.B
        bRamCounter.reset()
        bRamStat := BS.DATA_LOAD
        when(io.dChannel.in.rreq){
          bRamOutReg := sramReadWord(io.dChannel.in.addr(21,2))
        }.otherwise{
          bRamOutReg := sramReadWord(drBuf.addr(21,2))
        }
      }.elsewhen(inst2BaseLoad){
        isInst2BaseLoad := false.B
        iWordCounter.reset()
        bRamCounter.reset()
        bRamStat := BS.INST_LOAD
        when(io.iChannel.in.rreq){
          bRamOutReg := sramReadWord(pc2BaseAddr(io.iChannel.in.pc))
        }.otherwise{
          bRamOutReg := sramReadWord(pc2BaseAddr(irBuf.pc))
        }
      }
    }
    is(BS.INST_LOAD){
      when(iWordCounter.value === iWords.U){
        io.iChannel.out.rvalid := true.B
        io.iChannel.out.rdata := irData
        irStat := WS.IDLE
        bRamStat := BS.IDLE
      }.otherwise{
        when(bRamCounter.inc()){
          irData := io.baseRam.in.rData ## irData(iBandWidth-1,32)
          bRamOutReg.addr := bRamOutReg.addr + 1.U
          iWordCounter.inc()
        }
      }
    }
    is(BS.DATA_LOAD){
      when(bRamCounter.inc()){
        drStat := WS.IDLE
        io.dChannel.out.rvalid := true.B
        io.dChannel.out.rdata := byteSelInWords(drBuf.byteSelN,io.baseRam.in.rData)
        bRamStat := BS.IDLE
      }
    }
    is(BS.DATA_STORE){
      when(bRamCounter.inc()){
        dwStat := WS.IDLE
        io.dChannel.out.wdone := true.B
        bRamStat := BS.IDLE
      }
    }
  }

  /**
   * ExtSram Side Logic
   */
  switch(eRamStat){
    is(ES.IDLE){
      // Determined by drStat/dwStat
    }
    is(ES.LOAD){
      when(eRamCounter.inc()){
        drStat := WS.IDLE
        io.dChannel.out.rvalid := true.B
        io.dChannel.out.rdata := byteSelInWords(drBuf.byteSelN,io.extRam.in.rData)
        isData2ExtLoadBlock := false.B

        when(data2ExtStoreBlock){
          eRamStat := ES.STORE
          eRamCounter.reset()
          when(io.dChannel.in.wreq){
            eRamOutReg := sramWrite(
              io.dChannel.in.addr(21,2),
              io.dChannel.in.wdata,
              io.dChannel.in.byteSelN
            )
          }.otherwise{
            eRamOutReg := sramWrite(
              dwBuf.addr(21,2),
              dwBuf.wdata,
              dwBuf.byteSelN
            )
          }
        }.otherwise{
          eRamStat := ES.IDLE
        }
      }
    }
    is(ES.STORE){
      when(eRamCounter.inc()){
        dwStat := WS.IDLE
        io.dChannel.out.wdone := true.B
        isData2ExtStoreBlock := false.B
        when(data2ExtLoadBlock){
          eRamStat := ES.LOAD
          eRamCounter.reset()
          when(io.dChannel.in.rreq){
            eRamOutReg := sramReadWord(io.dChannel.in.addr(21,2))
          }.otherwise{
            eRamOutReg := sramReadWord(drBuf.addr(21,2))
          }
        }.otherwise{
          eRamStat := ES.IDLE
        }
      }
    }
  }

  /**
   * Uart Side Logic
   */
  switch(uStat){
    is(US.IDLE){
      uTstart := false.B
      uTdata := 0.U
      uRclear := false.B
    }
    is(US.LOAD){
      uStat := US.IDLE
      drStat := WS.IDLE
      io.dChannel.out.rvalid := true.B
      switch(drBuf.addr(3,0)){
        is(uartStatAddr){
          io.dChannel.out.rdata := uStatus
        }
        is(uartDataAddr){
          io.dChannel.out.rdata := uReceiver.io.RxD_data
          uRclear := true.B
        }
      }
    }
    is(US.STORE){
      uStat := US.IDLE
      dwStat := WS.IDLE
      io.dChannel.out.wdone := true.B
      uTstart := true.B
      uTdata := dwBuf.wdata(7,0)
    }
  }
}

object GammaBus {
  def pc2BaseAddr(pc:Bits) = {
    pc(21,iOffsetWidth) ## 0.U((iOffsetWidth -2).W)
  }

  def byteSelInWords(byteSelN: Bits, data: Bits): Bits = {
    val res = WireDefault(data)
    switch(byteSelN) {
      is("b1110".U) {
        res := data(7, 0)
      }
      is("b1101".U) {
        res := data(15, 8)
      }
      is("b1011".U) {
        res := data(23, 16)
      }
      is("b0111".U) {
        res := data(31, 24)
      }
      is("b0000".U) {
        res := data
      }
    }
    res
  }
  def va2deviceAddr(addr:Bits) = {
    addr(23,22)
  }
}
