package ultra.bus
import chisel3._
import chisel3.util._
import UltraBusPorts._
import UltraBusUtils._
import UltraBusParams._
import ultra.bus.uart.{async_receiver, async_transmitter}
import sram.SramUtils._

class UltraBus extends Module{
  val io = IO(new UltraBusIo)
  // ---------------------Uart Module----------------------------------
  // ------------------------------------------------------------------
  val UartTransmitter = Module(new async_transmitter(cpuFrequency))
  val UartReceiver = Module(new async_receiver(cpuFrequency))
  io.uart.txd := UartTransmitter.io.TxD
  UartReceiver.io.RxD := io.uart.rxd
  val UT_data = RegInit(0.U(8.W))
  val UT_start = RegInit(false.B)
  val UR_clear = RegInit(false.B)
  val U_status = WireDefault(
    UartReceiver.io.RxD_data_ready ##
      ~UartTransmitter.io.TxD_busy
  )
  UartTransmitter.io.clk := clock
  UartReceiver.io.clk := clock
  UartTransmitter.io.TxD_data := UT_data
  UartTransmitter.io.TxD_start := UT_start
  UartReceiver.io.RxD_clear := UR_clear
  // -------------------------------------------------------------------
  // InstChannel Signals and Utils
  // -------------------------------------------------------------------
  io.iChannel.out := initInstRspns
  val isInstHasReq = WireDefault(io.iChannel.in.rreq)
  val iReqReg = RegInit(initInstReq)
  import UltraBusUtils.{InstState => I}
  val istat = RegInit(I.IDLE)
  val iWordCnt = Counter(iWords+1)
  val iCycleCnt = Counter(memCycles)
  val idata = RegInit(0.U(iBandWidth.W))

  io.iChannel.out.rrdy := istat === I.IDLE
  def ackInstReq() = {
    iReqReg := io.iChannel.in
    idata := 0.U
    iWordCnt.reset()
    iCycleCnt.reset()
  }
  def pc2BaseAddr(pc:UInt) = {
    pc(21,iOffsetWidth) ## 0.U((iOffsetWidth-2).W)
  }
  def initIchannel() = {
    io.iChannel.out.rvalid := false.B
    io.iChannel.out.rdata := 0.U
  }
  // -------------------------------------------------------------------
  // Data Channel Signals and Utils
  // -------------------------------------------------------------------
  io.dChannel.out := initDataRspns
  val dHasReq = WireDefault(io.dChannel.in.rreq | io.dChannel.in.wreq)
  val dReqReg = RegInit(initDataReq)

  import UltraBusUtils.{DataState => D}
  val dstat = RegInit(D.IDLE)
  val isData2BaseRam = WireDefault(
    (dHasReq && io.dChannel.in.addr(31,22) === baseSramAddr) ||
      dstat === D.B_WAIT
  )
  val isData2ExtRam = WireDefault(
    (dHasReq && io.dChannel.in.addr(31,22) === extSramAddr) ||
      dstat === D.E_WAIT
  )
  val isData2Uart = WireDefault(
    dHasReq && io.dChannel.in.addr(31,24) === uartAddr
  )
  val dCycleCnt = Counter(memCycles)
  def byteSelInWords(byteSelN:UInt,data:UInt):UInt = {
    val res = WireDefault(data)
    switch(byteSelN){
      is("b1110".U){
        res := data(7,0)
      }
      is("b1101".U){
        res := data(15,8)
      }
      is("b1011".U){
        res := data(23,16)
      }
      is("b0111".U){
        res := data(31,24)
      }
      is("b0000".U){
        res := data
      }
    }
    res
  }
  def ackDataReq() = {
    dReqReg := io.dChannel.in
    dCycleCnt.reset()
  }
  def initDchannel() = {
    io.dChannel.out.rdata := 0.U
    io.dChannel.out.rvalid := false.B
    io.dChannel.out.wdone := false.B
    UR_clear := false.B
    UT_data := 0.U
    UT_start := false.B
  }
  def procBaseramReq(req:DataReq) = {
    when(req.rreq){
      dstat := D.B_LOAD
      baseramReqReg := sramRead(req.addr(21,2),req.byteSelN)
    }
    when(req.wreq){
      dstat := D.B_STORE
      baseramReqReg := sramWrite(req.addr(21,2),req.wdata,req.byteSelN)
    }
  }
  def procExtramReq(req:DataReq) = {
    when(req.rreq){
      dstat := D.E_LOAD
      extramReqReg := sramRead(req.addr(21,2),req.byteSelN)
    }
    when(req.wreq){
      dstat := D.E_STORE
      extramReqReg := sramWrite(req.addr(21,2),req.wdata,req.byteSelN)
    }
  }
  def procUartReq(req:DataReq) = {
    when(req.wreq){
      UT_start := true.B
      UT_data := io.dChannel.in.wdata(7,0)
      dstat := D.U_STORE
    }
    when(req.rreq){
      switch(req.addr(3,0)){
        is(uartStatAddr){
          dstat := D.U_CHECK
        }
        is(uartDataAddr){
          dstat := D.U_LOAD
        }
      }
    }
  }
  io.dChannel.out.rrdy := dstat === D.IDLE
  io.dChannel.out.wrdy := dstat === D.IDLE

  // -------------------------------------------------------------------
  // Base Sram Signals
  val baseramReqReg = RegInit(initSramReq)
  io.baseRam.out := baseramReqReg
  val isBaseramBusy = WireDefault(
    istat === I.B_LOAD ||
      dstat === D.B_LOAD ||
      dstat === D.B_STORE
  )
  // Ext Sram Signal
  val extramReqReg = RegInit(initSramReq)
  io.extRam.out := extramReqReg
  val extramBusy = WireDefault(
    dstat === D.E_STORE ||
      dstat === D.E_LOAD
  )
  // -------------------------------------------------------------------
  // Processing Instruction Request
  // -------------------------------------------------------------------
  switch(istat) {
    is (I.IDLE) {
      initIchannel()
      when(isInstHasReq) {
        ackInstReq()
        when(isData2BaseRam || isBaseramBusy) {
          istat := I.WAIT
        }.otherwise {
          istat := I.B_LOAD
          baseramReqReg := sramReadWord(pc2BaseAddr(io.iChannel.in.pc))
        }
      }
    }
    is (I.B_LOAD) {
      when(iWordCnt.value === iWords.U) {
        istat := I.IDLE
        io.iChannel.out.rvalid := true.B
        io.iChannel.out.rdata := idata
        baseramReqReg := initSramReq
      }.otherwise{
        when (iCycleCnt.inc()) {
          idata := io.baseRam.in.rData ## idata(iBandWidth-1,32)
          baseramReqReg.addr := baseramReqReg.addr + 1.U
          iWordCnt.inc()
        }
      }
    }
    is (I.WAIT) {
      when(isData2BaseRam || isBaseramBusy) {
        istat := I.WAIT
      }.otherwise{
        istat := I.B_LOAD
        baseramReqReg := sramReadWord(pc2BaseAddr(iReqReg.pc))
      }
    }
  }
  // -------------------------------------------------------------------
  // Processing Data Request
  // -------------------------------------------------------------------
  switch(dstat){
    is(D.IDLE){
      initDchannel()
      when(dHasReq){
        ackDataReq()
        when(isData2BaseRam){
          when(isBaseramBusy){
            dstat := D.B_WAIT
          }.otherwise{
            dstat := D.B_LOAD
            procBaseramReq(io.dChannel.in)
          }
        }
      }

      when(isData2ExtRam){
        when(extramBusy){
          dstat := D.E_WAIT
        }.otherwise{
          procExtramReq(io.dChannel.in)
        }
      }
      when(isData2Uart){
        procUartReq(io.dChannel.in)
      }
    }
    is(D.B_WAIT){
      when(!isBaseramBusy){
        procBaseramReq(dReqReg)
      }
    }
    is(D.B_STORE){
      when(dCycleCnt.inc()){
        dstat := D.IDLE
        io.dChannel.out.wdone := true.B
        baseramReqReg := initSramReq
      }
    }
    is(D.B_LOAD){
      when(dCycleCnt.inc()){
        dstat := D.IDLE
        io.dChannel.out.rvalid := true.B
        io.dChannel.out.rdata := byteSelInWords(dReqReg.byteSelN,io.baseRam.in.rData)
        baseramReqReg := initSramReq
      }
    }
    is(D.E_WAIT){
      when(!extramBusy){
        procExtramReq(dReqReg)
      }
    }
    is(D.E_STORE){
      when(dCycleCnt.inc()){
        dstat := D.IDLE
        io.dChannel.out.wdone := true.B
        extramReqReg := initSramReq
      }
    }
    is(D.E_LOAD){
      when(dCycleCnt.inc()){
        dstat := D.IDLE
        io.dChannel.out.rvalid := true.B
        io.dChannel.out.rdata := byteSelInWords(dReqReg.byteSelN,io.extRam.in.rData)
        extramReqReg := initSramReq
      }
    }
    is(D.U_CHECK){
      dstat := D.IDLE
      io.dChannel.out.rvalid := true.B
      io.dChannel.out.rdata := U_status
    }
    is(D.U_LOAD){
      dstat := D.IDLE
      io.dChannel.out.rvalid := true.B
      io.dChannel.out.rdata := UartReceiver.io.RxD_data
      UR_clear := true.B
    }
    is(D.U_STORE){
      dstat := D.IDLE
      io.dChannel.out.wdone := true.B
    }
  }
  // -------------------------------------------------------------------
}
