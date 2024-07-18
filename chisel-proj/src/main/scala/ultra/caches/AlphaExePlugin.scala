package ultra.caches
import chisel3._
import chisel3.util._
import ultra.bus.UltraBusUtils._
import ultra.bus.UltraBusPorts._
import ultra.pipeline.exe.UltraExePorts.ExeAsidePorts._
import ultra.pipeline.exe.UltraExeUtils._
import ultra.bus.UltraBusUtils.DataLoadType._
import ultra.pipeline.exe.UltraExeParams._
class AlphaExePlugin extends Module {
  val io = IO(new Bundle {
    val core = new ExeAsideSlaveIo
    val bus = new DataMasterIo
  })
  val rspns2coreRef = io.core.out
  val req2busRef = io.bus.out
  def default() = {
    rspns2coreRef := initExeAsideIn
    req2busRef := initDataReq
  }
  default()
  object ExePluginState extends ChiselEnum{
    val
    IDLE,
    LOADING,
    STORE_CACHE,
    STORING
    = Value
  }
  import ExePluginState._
  val expStat = RegInit(IDLE)

  /**
   * Believe me,it would work
   * the rvalid and wdone signal from bus is synchronous,that is,
   * when plugin receive one of them,the bus dstat is IDLE,which can process another req
   */
  rspns2coreRef.rrdy :=
    (expStat === IDLE) ||
      (expStat === LOADING && io.bus.in.rvalid) ||
      (expStat === STORING && io.bus.in.wdone)
  rspns2coreRef.wrdy :=
    (expStat === IDLE) ||
      (expStat === STORING && io.bus.in.wdone) ||
      (expStat === LOADING && io.bus.in.rvalid)

  def processStore() = {
    expStat := STORE_CACHE
    req2busRef.wreq := true.B
    req2busRef.addr := io.core.in.addr
    req2busRef.wdata := io.core.in.wdata
  }
  def processLoad() = {
    expStat := LOADING
    req2busRef.byteSelN := io.core.in.byteSelN
    req2busRef.rreq := true.B
    req2busRef.addr := io.core.in.addr
    req2busRef.rtype := io.core.in.uncache
  }
  def processReq(coreReq:ExeAsideOut) = {
    when(coreReq.rreq){
      processLoad()
    }
    when(coreReq.wreq){
      processStore()
    }
  }
  switch(expStat){
    is(IDLE){
      processReq(io.core.in)
    }
    is(STORE_CACHE){
      expStat := STORING
      rspns2coreRef.wdone := true.B
      when(io.bus.in.wdone){
        expStat := IDLE
      }
    }
    is(STORING){
      when(io.bus.in.wdone){
        expStat := IDLE
        processReq(io.core.in)
      }
    }
    is(LOADING){
      when(io.bus.in.rvalid){
        expStat := IDLE
        rspns2coreRef.rvalid := io.bus.in.rvalid
        rspns2coreRef.rdata := io.bus.in.rdata(31,0)
        processReq(io.core.in)
      }
    }
  }
}
