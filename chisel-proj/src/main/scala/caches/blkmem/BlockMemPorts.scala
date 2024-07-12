package caches.blkmem

import chisel3._
import chisel3.util._

object BlockMemPorts {
  class BlockMemIn(addrWidth:Int,dataWidth:Int) extends Bundle {
    val wen = Bool()
    val wdata = UInt(dataWidth.W)
    val addr = UInt(addrWidth.W)
  }
  class BlockMemOut(dataWidth:Int) extends Bundle {
    val rdata = UInt(dataWidth.W)
  }
}
