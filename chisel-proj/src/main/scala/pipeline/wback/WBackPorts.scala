package pipeline.wback
import chisel3._
import pipeline.regfile.RegfilePorts.{WriteCtrl => RegWriteCtrl}

object WBackPorts {
  class WBackReq extends Bundle {
    val req = Bool()
    val bits = new RegWriteCtrl
  }
  class WBackOut extends Bundle {
    val ack = Bool()
    val bits = new RegWriteCtrl
  }
  class WBackIo extends Bundle {
    val in = Input(new WBackReq)
    val out = Output(new WBackOut)
  }
}