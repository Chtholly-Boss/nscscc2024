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
  // -------------------------------------------------------------------
  // InstChannel Signals and Utils
  // -------------------------------------------------------------------
  val isInstHasReq = WireDefault(io.iChannel.in.rreq)
  val iReqReg = RegInit(initInstReq)
  when(isInstHasReq) {
    iReqReg := io.iChannel.in
  }
  val iRspnsReg = RegInit(initInstRspns)
  io.iChannel.out := iRspnsReg

  import UltraBusUtils.{InstState => I}
  val istat = RegInit(I.IDLE)

  val iWordCnt = Counter(iWords+1)
  val iCycleCnt = Counter(memCycles)
  val idata = RegInit(0.U(iBandWidth.W))
  def istat2Load(addr: Bits): Unit = {
    istat := I.B_LOAD
    idata := 0.U
    iWordCnt.reset()
    iCycleCnt.reset()
    baseramReqReg := sramReadWord(addr)
  }
  def instIsProcessing():Unit = {
    iRspnsReg.rrdy := false.B
    iRspnsReg.rvalid := false.B
  }
  def iLoadDone(rdata:Bits):Unit = {
    iRspnsReg.rrdy := true.B
    iRspnsReg.rvalid := true.B
    iRspnsReg.rdata := rdata
    istat := I.IDLE
  }
  // -------------------------------------------------------------------
  // Data Channel Signals and Utils
  // -------------------------------------------------------------------
  val dHasReq = WireDefault(
    io.dChannel.in.rreq | io.dChannel.in.wreq
  )
  val dReqReg = RegInit(initDataReq)
  when(dHasReq) {
    dReqReg := io.dChannel.in
  }
  val dRspnsReg = RegInit(initDataRspns)
  io.dChannel.out := dRspnsReg

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
  val dWordCnt = Counter(dWords+1)
  val dCycleCnt = Counter(memCycles)
  val dData = RegInit(0.U(dBandWidth.W))

  def dIsProcessing():Unit = {
    dRspnsReg.rrdy := false.B
    dRspnsReg.wrdy := false.B
    dRspnsReg.wdone := false.B
    dRspnsReg.rvalid := false.B
  }
  def dLoadDone(rdata:Bits):Unit = {
    dstat := D.IDLE
    dRspnsReg.rrdy := true.B
    dRspnsReg.wrdy := true.B
    dRspnsReg.rvalid := true.B
    dRspnsReg.rdata := rdata
  }
  def dStoreDone():Unit = {
    dstat := D.IDLE
    dRspnsReg.rrdy := true.B
    dRspnsReg.wrdy := true.B
    dRspnsReg.rvalid := false.B
    dRspnsReg.wdone := true.B
    dRspnsReg.rdata := 0.U
  }
  def dstat2BaseLoad(src:DataReq) = {
    dstat := D.B_LOAD
    baseramReqReg := sramRead(src.addr(21,2),src.byteSelN)
    dWordCnt.reset()
    dCycleCnt.reset()
    dData := 0.U
  }
  def dstat2BaseStore(src:DataReq) = {
    dstat := D.B_STORE
    dCycleCnt.reset()
    baseramReqReg := sramWrite(src.addr(21,2),src.wdata,src.byteSelN)
  }
  def dstat2ExtLoad(src:DataReq)= {
    dstat := D.E_LOAD
    extramReqReg := sramRead(src.addr(21,2),src.byteSelN)
    dWordCnt.reset()
    dCycleCnt.reset()
    dData := 0.U
  }
  def dstat2ExtStore(src:DataReq):Unit = {
    dstat := D.E_STORE
    dCycleCnt.reset()
    extramReqReg := sramWrite(src.addr(21,2),src.wdata,src.byteSelN)
  }
  def procUartReq() = {
    when(io.dChannel.in.wreq){
      UT_start := true.B
      UT_data := io.dChannel.in.wdata(7,0)
      dStoreDone()
    }
    when(io.dChannel.in.rreq) {
      switch(io.dChannel.in.addr(3, 0)) {
        is(uartStatAddr) {
          dLoadDone(U_status)
        }
        is(uartDataAddr) {
          dLoadDone(UartReceiver.io.RxD_data)
          UR_clear := true.B
        }
      }
    }
  }
  def procBaseramReq(req:DataReq) = {
    when(req.rreq) {
      dstat2BaseLoad(req)
    }
    when(req.wreq) {
      dstat2BaseStore(req)
    }
  }
  def procExtramReq(req: DataReq) = {
    when(req.rreq){
      dstat2ExtLoad(req)
    }
    when(req.wreq){
      dstat2ExtStore(req)
    }
  }
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
      iRspnsReg := initInstRspns
      when(isInstHasReq) {
        instIsProcessing()
        when(isData2BaseRam || isBaseramBusy) {
          istat := I.WAIT
        }.otherwise {
          istat2Load(io.iChannel.in.pc(21,iOffsetWidth) ## 0.U((iOffsetWidth-2).W))
        }
      }
    }
    is (I.B_LOAD) {
      when(iWordCnt.value === iWords.U) {
        iLoadDone(idata)
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
        istat2Load(iReqReg.pc(21,iOffsetWidth) ## 0.U((iOffsetWidth-2).W))
      }
    }
  }
  // -------------------------------------------------------------------
  // Processing Data Request
  // -------------------------------------------------------------------
  switch(dstat){
    is(D.IDLE){
      dRspnsReg := initDataRspns
      UR_clear := false.B
      UT_start := false.B
      UT_data := 0.U
      when(isData2BaseRam){
        dIsProcessing()
        when(isBaseramBusy){
          dstat := D.B_WAIT
        }.otherwise{
          procBaseramReq(io.dChannel.in)
        }
      }
      when(isData2ExtRam){
        dIsProcessing()
        when(extramBusy){
          dstat := D.E_WAIT
        }.otherwise{
          procExtramReq(io.dChannel.in)
        }
      }
      when(isData2Uart){
        dIsProcessing()
        procUartReq()
      }
    }
    is(D.B_WAIT){
      when(!isBaseramBusy){
        procBaseramReq(dReqReg)
      }
    }
    is(D.B_STORE){
      when(dCycleCnt.inc()){
        dStoreDone()
        baseramReqReg := initSramReq
      }
    }
    is(D.B_LOAD){
      when(dCycleCnt.inc()){
        dLoadDone(io.baseRam.in.rData)
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
        dStoreDone()
        extramReqReg := initSramReq
      }
    }
    is(D.E_LOAD){
      when(dCycleCnt.inc()){
        dLoadDone(io.extRam.in.rData)
        extramReqReg := initSramReq
      }
    }
  }
  // -------------------------------------------------------------------
}
