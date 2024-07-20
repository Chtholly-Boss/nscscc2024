package ultra.pipeline.exe
import chisel3._
import UltraExeParams._
import ultra.pipeline.regfile.RegfilePorts._
import ultra.pipeline.decode.UltraDecodePorts.DecodePipePorts._
import ultra.bus.UltraBusPorts._
object UltraExePorts {
  object AluPorts {
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
  }
  object ExePipePorts {
    class ExeSrcInfo extends Bundle {
      val exeOp = new AluPorts.ExeOp
      val wCtrl = new WriteCtrl
      val operands = new Bundle() {
        val hasImm = Bool()
        val imm = UInt(dataWidth)
        val regData_1 = UInt(dataWidth)
        val regData_2 = UInt(dataWidth)
      }
    }
    class ExeOut extends Bundle {
      val bits = new WriteCtrl
    }
    class ExeBranchInfo extends Bundle {
      val isMispredict = Bool()
      val npc = UInt(dataWidth)
    }
    class ExePipeIo extends Bundle {
      val decode = new Bundle {
        val in = Input(new DecodePipeOut)
        val out = Output(new Bundle {
          val ack = Bool()
        })
      }
      val wback = new Bundle {
        val out = Output(new ExeOut)
      }
      val br = Output(new ExeBranchInfo)
    }
  }

  object ExeAsidePorts {
    class ExeAsideIn extends Bundle {
      val rdata = UInt(dataWidth)
      val rrdy = Bool()
      val rvalid = Bool()
      val wrdy = Bool()
      val wdone = Bool()
    }
    class ExeAsideOut extends Bundle {
      val rreq = Bool()
      val wreq = Bool()
      val addr = UInt(32.W)
      val byteSelN = UInt(32.W)
      val wdata = UInt(32.W)
    }
    class ExeAsideMasterIo extends Bundle {
      val in = Input(new ExeAsideIn)
      val out = Output(new ExeAsideOut)
    }
    class ExeAsideSlaveIo extends Bundle {
      val in = Input(new ExeAsideOut)
      val out = Output(new ExeAsideIn)
    }
  }


  class ExeIo extends Bundle {
    val pipe = new ExePipePorts.ExePipeIo
    val aside = new ExeAsidePorts.ExeAsideMasterIo
  }
  // This Aside Io will directly connect to the bus
  class AlphaExeIo extends Bundle {
    val pipe = new ExePipePorts.ExePipeIo
    val aside = new Bundle() {
      val in = Input(new DataRspns)
      val out = Output(new DataReq)
    }
  }
}
