package pipeline.exe
import chisel3._
import ExeParams._
import pipeline.regfile.RegfilePorts.WriteCtrl
object ExePorts {
  class ExeOp extends Bundle {
    val opType = UInt(typeWidth)
    val op = UInt(opWidth)
  }
  class ExeReq extends Bundle {
    val req = Bool()
    val exeOp = new ExeOp
    val opLeft = UInt()
    val opRight = UInt()
  }
  class ExeOut extends Bundle {
    val ack = Bool()
    val req = Bool()
    val bits = new WriteCtrl
  }
  class ExeIo extends Bundle {
    val in = Input(new ExeReq)
    val out = Output(new ExeOut)
  }
}