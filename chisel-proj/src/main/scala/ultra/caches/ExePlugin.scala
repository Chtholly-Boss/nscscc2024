package ultra.caches
import chisel3._
import chisel3.util._
import ultra.bus.UltraBusUtils._
import ultra.bus.UltraBusPorts._
import ultra.pipeline.exe.UltraExePorts.ExeAsidePorts._
import ultra.pipeline.exe.UltraExeUtils._
import ultra.bus.UltraBusUtils.DataLoadType._
import ultra.pipeline.exe.UltraExeParams._
class ExePlugin extends Module {
  val io = IO(new Bundle {
    val core = new ExeAsideSlaveIo
    val bus = new DataMasterIo
  })
  io.core.out.rrdy := io.bus.in.rrdy
  io.core.out.wrdy := io.bus.in.wrdy
  io.core.out.rdata := io.bus.in.rdata(31,0)
  io.core.out.rvalid := io.bus.in.rvalid
  io.core.out.wdone := io.bus.in.wdone

  io.bus.out.wreq := io.core.in.wreq
  io.bus.out.rreq := io.core.in.rreq
  io.bus.out.rtype := 1.U
  io.bus.out.addr := io.core.in.addr
  io.bus.out.wdata := io.core.in.wdata
  io.bus.out.byteSelN := io.core.in.byteSelN
}
