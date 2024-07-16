package ultra.pipeline.regfile

import chisel3._
import RegfileParam._

object RegfilePorts {
  class ReadCtrl extends Bundle {
    val addr = UInt(addrWidth)
  }
  class WriteCtrl extends Bundle {
    val en = Bool()
    val addr = UInt(addrWidth)
    val data = UInt(dataWidth)
  }
  class ReadChannel extends Bundle {
    val in = Input(new ReadCtrl)
    val out = Output(UInt(dataWidth))
  }
  class RegfileIo extends Bundle {
    val rChannel_1 = new ReadChannel
    val rChannel_2 = new ReadChannel
    val wChannel = Input(new WriteCtrl)
  }
}