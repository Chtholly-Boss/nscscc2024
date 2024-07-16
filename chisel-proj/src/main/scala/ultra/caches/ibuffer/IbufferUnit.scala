package ultra.caches.ibuffer

import chisel3._
import IbufferPorts._
import IbufferUtils._
import ultra.caches.icache.IcacheParams._
class IbufferUnit extends Module {
  val io = IO(new IbufferUnitIo)
  io.out.bits := initInstOut()
  io.out.isMatched := io.in.pc(offsetWidth-1,2) === io.in.index
  val buf = RegInit(initInstOut())
  io.out.bits := buf
  when(io.in.rvalid){
    // Update the buffer
    buf.inst := io.in.inst
    buf.rvalid := true.B
  }.otherwise {
    buf.rvalid := false.B
  }
}
