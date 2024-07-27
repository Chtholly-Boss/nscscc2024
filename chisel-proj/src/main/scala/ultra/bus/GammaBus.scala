package ultra.bus
import chisel3._
import chisel3.util._
import UltraBusPorts._
import UltraBusUtils._
import UltraBusParams._
import ultra.bus.uart.{async_receiver, async_transmitter}
import sram.SramUtils._
import ultra.bus.GammaBus.{byteSelInWords, pc2BaseAddr}
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
      INST_LOAD
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

  val isBramBusy = WireDefault(!(bRamStat === BS.IDLE))
  val isEramBusy = WireDefault(!(eRamStat === ES.IDLE))

  /**
   * Buffers:
   * - drBuf: data load request buffer
   * - dwBuf: data store request buffer
   */
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
   * TODO: Need to Handle Structure Hazard
   */
  switch(irStat){
    is(WS.IDLE){
      when(io.iChannel.in.rreq){
        irStat := WS.WAIT
        iWordCounter.reset()
        bRamCounter.reset()
        bRamOutReg := sramReadWord(pc2BaseAddr(io.iChannel.in.pc))
        bRamStat := BS.INST_LOAD
      }
    }
    is(WS.WAIT){
      // Determined by (bRamStat === InstLoad)
    }
  }

  /**
   * Data Channel Side Logic
   * TODO: Need to Support BaseRam Load/Store
   */
  val isData2ExtLoadBlock = RegInit(false.B)
  val isData2ExtStoreBlock = RegInit(false.B)

  switch(drStat){
    is(WS.IDLE){
      when(io.dChannel.in.rreq){
        drStat := WS.WAIT
        drBuf := io.dChannel.in
        when(io.dChannel.in.addr(31,22) === extSramAddr){
          when(isEramBusy){
            isData2ExtLoadBlock := true.B
          }.otherwise{
            eRamCounter.reset()
            eRamOutReg := sramRead(io.dChannel.in.addr(21,2),io.dChannel.in.byteSelN)
            eRamStat := ES.LOAD
          }
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
        when(io.dChannel.in.addr(31,22) === extSramAddr){
          when(isEramBusy){
            isData2ExtStoreBlock := true.B
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
      // Determined by irStat/drStat/dwStat
    }
    is(BS.INST_LOAD){
      when(iWordCounter.value === iWords.U){
        io.iChannel.out.rvalid := true.B
        io.iChannel.out.rdata := irData
        irStat := WS.IDLE
        bRamStat := BS.IDLE // TODO
      }.otherwise{
        when(bRamCounter.inc()){
          irData := io.baseRam.in.rData ## irData(iBandWidth-1,32)
          bRamOutReg.addr := bRamOutReg.addr + 1.U
          iWordCounter.inc()
        }
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

        when(isData2ExtStoreBlock){
          eRamStat := ES.STORE
          isData2ExtStoreBlock := false.B
          eRamCounter.reset()
          eRamOutReg := sramWrite(
            dwBuf.addr(21,2),
            dwBuf.wdata,
            dwBuf.byteSelN
          )
        }.otherwise{
          eRamStat := ES.IDLE
        }
      }
    }
    is(ES.STORE){
      when(eRamCounter.inc()){
        dwStat := WS.IDLE
        io.dChannel.out.wdone := true.B

        when(isData2ExtLoadBlock){
          eRamStat := ES.LOAD
          isData2ExtLoadBlock := false.B
          eRamCounter.reset()
          eRamOutReg := sramRead(drBuf.addr(21,2),drBuf.byteSelN)
        }.otherwise{
          eRamStat := ES.IDLE
        }
      }
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
}
