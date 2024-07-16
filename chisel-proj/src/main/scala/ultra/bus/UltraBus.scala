package ultra.bus
import chisel3._
import chisel3.util._
import UltraBusPorts._
import UltraBusUtils._
import UltraBusParams._
import ultra.bus.uart.{async_receiver, async_transmitter}
import sram.SramUtils._

class UltraBus extends Module{
  val io = IO(new Bundle() {
    val iChannel = new InstSlaveIo
    val dChannel = new DataSlaveIo
    val baseRam = new SramMasterIo
    val extRam = new SramMasterIo
    val uart = new UartMasterIo
  })
  // --- Uart ---
  val UartTransmitter = Module(new async_transmitter(cpuFrequency))
  val UartReceiver = Module(new async_receiver(cpuFrequency))
  io.uart.txd := UartTransmitter.io.TxD
  UartReceiver.io.RxD := io.uart.rxd
  val UT_data = WireDefault(0.U(8.W))
  val UT_start = WireDefault(false.B)
  val UR_clear = WireDefault(false.B)
  val U_status = WireDefault(
    UartReceiver.io.RxD_data_ready ##
      ~UartTransmitter.io.TxD_busy
  )
  UartTransmitter.io.clk := clock
  UartReceiver.io.clk := clock
  UartTransmitter.io.TxD_data := UT_data
  UartTransmitter.io.TxD_start := UT_start
  UartReceiver.io.RxD_clear := UR_clear
  // Signals to Communicate with BaseRam and ExtRam
  val baseramReqReg = RegInit(initSramReq)
  val extramReqReg = RegInit(initSramReq)
  io.baseRam.out := baseramReqReg
  io.extRam.out := extramReqReg
  val baseramInWire = WireDefault(io.baseRam.in)
  val extramInWire = WireDefault(io.extRam.in)
  // Signals to Communicate with FetchStage and ExeStage
  val iHasReq = WireDefault(io.iChannel.in.rreq)
  val iReqReg = RegInit(initInstReq)
  when(iHasReq) {
    iReqReg := io.iChannel.in
  }
  val iRspnsReg = RegInit(initInstRspns)

  val dHasReq = WireDefault(
    io.dChannel.in.rreq | io.dChannel.in.wreq
  )
  val dReqReg = RegInit(initDataReq)
  when(dHasReq) {
    dReqReg := io.dChannel.in
  }
  val dRspnsReg = RegInit(initDataRspns)
  io.iChannel.out := iRspnsReg
  io.dChannel.out := dRspnsReg
  // Instruction State Machine
  import UltraBusUtils.{InstState => I}
  val istat = RegInit(I.IDLE)
  // Data State Machine
  import UltraBusUtils.{DataState => D}
  val dstat = RegInit(D.IDLE)
  // Status Signals
  val baseramBusy = WireDefault(
    istat === I.B_LOAD ||
      dstat === D.B_LOAD_SINGLE ||
      dstat === D.B_LOAD_LINE ||
      dstat === D.B_STORE
  )
  val extramBusy = WireDefault(
    dstat === D.E_STORE ||
      dstat === D.E_LOAD_LINE ||
      dstat === D.E_LOAD_SINGLE
  )
  val isData2BaseRam = WireDefault(
    dHasReq && io.dChannel.in.addr(31,22) === baseSramAddr
  )
  val isData2ExtRam = WireDefault(
    dHasReq && io.dChannel.in.addr(31,22) === extSramAddr
  )
  val isData2Uart = WireDefault(
    dHasReq && io.dChannel.in.addr(31,24) === uartAddr
  )

  // Instruction Handy Functions
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
  // Instruction State Machine Logic
  switch(istat) {
    is (I.IDLE) {
      iRspnsReg := initInstRspns
      when(iHasReq) {
        instIsProcessing()
        when(isData2BaseRam || baseramBusy) {
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
          idata := baseramInWire.rData ## idata(iBandWidth-1,32)
          baseramReqReg.addr := baseramReqReg.addr + 1.U
          iWordCnt.inc()
        }
      }
    }
    is (I.WAIT) {
      when(isData2BaseRam || baseramBusy) {
        istat := I.WAIT
      }.otherwise{
        istat2Load(iReqReg.pc(21,iOffsetWidth) ## 0.U((iOffsetWidth-2).W))
      }
    }
  }


  // Data Handy Functions
  import DataLoadType._
  val dWordCnt = Counter(dWords+1)
  val dCycleCnt = Counter(memCycles)
  val dData = RegInit(0.U(dBandWidth.W))
  // Data State Machine Logic
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
    switch(src.rtype){
      is(1.U) { // uncache
        dstat := D.B_LOAD_SINGLE
        baseramReqReg := sramRead(src.addr(21,2),src.byteSelN)
      }
      is(0.U) { // cache
        dstat := D.B_LOAD_LINE
        baseramReqReg := sramRead(src.addr(21,dOffsetWidth) ## 0.U((dOffsetWidth-2).W),src.byteSelN)
      }
    }
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
    switch(src.rtype) {
      is(1.U){
        dstat := D.E_LOAD_SINGLE
        extramReqReg := sramRead(src.addr(21,2),src.byteSelN)
      }
      is(0.U) {
        dstat := D.E_LOAD_LINE
        extramReqReg := sramRead(src.addr(21,dOffsetWidth) ## 0.U((dOffsetWidth-2).W),src.byteSelN)
      }
    }
    dWordCnt.reset()
    dCycleCnt.reset()
    dData := 0.U
  }
  def dstat2ExtStore(src:DataReq):Unit = {
    dstat := D.E_STORE
    dCycleCnt.reset()
    extramReqReg := sramWrite(src.addr(21,2),src.wdata,src.byteSelN)
  }

  switch(dstat){
    is(D.IDLE){
      dRspnsReg := initDataRspns
      UR_clear := false.B
      UT_start := false.B
      when(isData2BaseRam){
        dIsProcessing()
        when(baseramBusy){
          dstat := D.B_WAIT
        }.otherwise{
          when(io.dChannel.in.rreq) {
            dstat2BaseLoad(io.dChannel.in)
          }
          when(io.dChannel.in.wreq) {
            dstat2BaseStore(io.dChannel.in)
          }

        }
      }
      when(isData2ExtRam){
        dIsProcessing()
        when(extramBusy){
          dstat := D.E_WAIT
        }.otherwise{
          when(io.dChannel.in.rreq){
            dstat2ExtLoad(io.dChannel.in)
          }
          when(io.dChannel.in.wreq){
            dstat2ExtStore(io.dChannel.in)
          }
        }
      }
      when(isData2Uart){
        dIsProcessing()
        switch(io.dChannel.in.addr(3,0)){
          is(uartStatAddr){
            dLoadDone(U_status)
          }
          is(uartDataAddr){
            when(io.dChannel.in.rreq){
              UR_clear := true.B
              dLoadDone(UartReceiver.io.RxD_data)
            }
            when(io.dChannel.in.wreq){
              UT_start := true.B
              UT_data := io.dChannel.in.wdata(7,0)
              dStoreDone()
            }
          }
        }
      }
    }
    is(D.B_WAIT){
      when(!baseramBusy){
        when(dReqReg.rreq) {
          dstat2BaseLoad(dReqReg)
        }
        when(dReqReg.wreq) {
          dstat2BaseStore(dReqReg)
        }
      }
    }
    is(D.B_STORE){
      when(dCycleCnt.inc()){
        dStoreDone()
        baseramReqReg := initSramReq
      }
    }
    is(D.B_LOAD_SINGLE){
      when(dCycleCnt.inc()){
        dLoadDone(baseramInWire.rData)
        baseramReqReg := initSramReq
      }
    }
    is(D.B_LOAD_LINE){
      when(dWordCnt.value === dWords.U){
        dLoadDone(dData)
        baseramReqReg := initSramReq
      }.otherwise{
        when(dCycleCnt.inc()){
          dData := baseramInWire.rData ## dData(dBandWidth-1,32)
          baseramReqReg.addr := baseramReqReg.addr + 1.U
          dWordCnt.inc()
        }
      }
    }
    is(D.E_WAIT){
      when(!extramBusy){
        when(dReqReg.rreq){
          dstat2ExtLoad(dReqReg)
        }
        when(dReqReg.wreq){
          dstat2ExtStore(dReqReg)
        }
      }
    }
    is(D.E_STORE){
      when(dCycleCnt.inc()){
        dStoreDone()
        extramReqReg := initSramReq
      }
    }
    is(D.E_LOAD_SINGLE){
      when(dCycleCnt.inc()){
        dLoadDone(extramInWire.rData)
        extramReqReg := initSramReq
      }
    }
    is(D.E_LOAD_LINE){
      when(dWordCnt.value === dWords.U){
        dLoadDone(dData)
        extramReqReg := initSramReq
      }.otherwise{
        when(dCycleCnt.inc()){
          dData := extramInWire.rData ## dData(dBandWidth-1,32)
          extramReqReg.addr := extramReqReg.addr + 1.U
          dWordCnt.inc()
        }
      }
    }
  }
}
