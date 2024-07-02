package sram

import chisel3._
import SramParam._

class SramResponse extends Bundle {
  val rData = UInt(dataWidth)
}
