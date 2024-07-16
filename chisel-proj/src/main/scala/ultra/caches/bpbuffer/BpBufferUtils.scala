package ultra.caches.bpbuffer

import chisel3._
import BpBufferPorts._
object BpBufferUtils {
  def initBpOut():BpOut = {
    val init = Wire(new BpOut)
    init.predictTaken := false.B
    init.offset := 4.U
    init
  }
}
