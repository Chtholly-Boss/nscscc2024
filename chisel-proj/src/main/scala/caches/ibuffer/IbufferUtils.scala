package caches.ibuffer
import chisel3._
import IbufferPorts._
object IbufferUtils {
  def initInstOut(): InstOut = {
    val init = Wire(new InstOut)
    init.inst := 0.U
    init.rvalid := false.B
    init
  }
}
