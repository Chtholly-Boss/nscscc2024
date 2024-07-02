package sram

import chisel3._
import sram.SramParam._

class SramRequest extends Bundle {
  val wData = UInt(dataWidth)
  val addr = UInt(addrWidth)
  val byteSelN = UInt(byteSelWidth)
  val ce = UInt()
  val oe = UInt()
  val we = UInt()
}