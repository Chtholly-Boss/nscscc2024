package ultra.caches
import chisel3._
import ultra.bus.UltraBusUtils._
import ultra.bus.UltraBusPorts._
class ExePlugin extends Module {
  val io = IO(new Bundle {
    val core = new DataSlaveIo
    val bus = new DataMasterIo
  })

  val rspns2coreReg = RegInit(initDataRspns)
  val req2busReg = RegInit(initDataReq)
  io.core.out := rspns2coreReg
  io.bus.out := req2busReg

  val wReqBuf = RegInit(initDataReq)
  when(io.core.in.wreq){
    wReqBuf := io.core.in
  }

  val rReqBuf = RegInit(initDataReq)
  when(io.core.in.rreq){
    rReqBuf := io.core.in
  }


}
