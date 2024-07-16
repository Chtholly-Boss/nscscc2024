package ultra.bus.sram

import chisel3._
import SramParam._

object SramPorts {
  class SramRequest extends Bundle {
    val wData = UInt(dataWidth)
    val addr = UInt(addrWidth)
    val byteSelN = UInt(byteSelWidth)
    val ce = UInt()
    val oe = UInt()
    val we = UInt()
  }
  class SramResponse extends Bundle {
    val rData = UInt(dataWidth)
  }
  class SramIO extends Bundle {
    val in = Input(new SramRequest)
    val out = Output(new SramResponse)
  }
}