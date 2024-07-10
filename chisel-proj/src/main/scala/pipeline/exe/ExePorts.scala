package pipeline.exe
import chisel3._
import ExeParams._
import pipeline.regfile.RegfilePorts.WriteCtrl
import pipeline.decode.DecodePorts.DecodeOut

object ExePorts {
  class Operands extends Bundle {
    val left = UInt(dataWidth)
    val right = UInt(dataWidth)
  }
  class ExeOp extends Bundle {
    val opType = UInt(typeWidth)
    val opFunc = UInt(opWidth)
  }
  // Alu Ports: to calculate a result
  class AluIo extends Bundle {
    val in = Input(new Bundle {
      val op = new ExeOp
      val operand = new Operands
    })
    val out = Output(new Bundle() {
      val res = UInt(dataWidth)
    })
  }
  // Exe IO: to cooperate in the pipeline
  class ExeSrcInfo extends Bundle {
    val exeOp = new ExeOp
    val wCtrl = new WriteCtrl
    val operands = new Bundle() {
      val hasImm = Bool()
      val imm = UInt(dataWidth)
      val regData_1 = UInt(dataWidth)
      val regData_2 = UInt(dataWidth)
    }
  }

  class ExeOut extends Bundle {
    val req = Bool()
    val bits = new WriteCtrl
  }
  class ExeIo extends Bundle {
    val in = Input(new Bundle() {
      val decode = new DecodeOut
      val ack = Bool()
    })
    val out = Output(new Bundle {
      val ack = Bool()
      val exe = new ExeOut
    })
  }

}