package ultra.bus.sram

import chisel3._

object SramParam {
  val dataWidth = 32.W
  val addrWidth = 20.W
  val byteSelWidth = 4.W

  val wordLength = 32
  val depth = 1 << 20
  val highLevel = 1.U
  val lowLevel = 0.U
}
