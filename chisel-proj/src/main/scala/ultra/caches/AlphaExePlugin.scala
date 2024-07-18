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

  rspns2coreRef.rrdy := expStat === IDLE
  rspns2coreRef.wrdy := expStat === IDLE
    switch(expStat){
    is(IDLE){
      when(io.core.in.rreq){
        expStat := LOADING
        req2busRef.byteSelN := io.core.in.byteSelN
        req2busRef.rreq := true.B
        req2busRef.addr := io.core.in.addr
        req2busRef.rtype := io.core.in.uncache
      }
      when(io.core.in.wreq){
        expStat := STORE_CACHE
        req2busRef.wreq := true.B
        req2busRef.addr := io.core.in.addr
        req2busRef.wdata := io.core.in.wdata
      }
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
      }
    }
    is(LOADING){
      when(io.bus.in.rvalid){
        expStat := IDLE
        rspns2coreRef.rvalid := io.bus.in.rvalid
        rspns2coreRef.rdata := io.bus.in.rdata(31,0)
      }
    }
  }
}
