package ultra.pipeline.decode
import chisel3._
import ultra.pipeline.regfile.RegfilePorts._
import ultra.pipeline.exe.UltraExePorts._
import ultra.pipeline.fetch.UltraFetchPorts.FetchPipePorts._
import UltraDecodeParams._
object UltraDecodePorts {
  object DecoderPorts {
    class DecoderRes extends Bundle {
      val exeOp = new ExeOp
      val hasImm = Bool()
      val imm = UInt(immWidth)
      val reg_1 = new ReadCtrl
      val reg_2 = new ReadCtrl
      val wCtrl = new WriteCtrl
    }
    class DecoderIo extends Bundle {
      val in = Input(new DecodePipePorts.DecodeSrcInfo)
      val out = Output(new Bundle {
        val isMatched = Bool()
        val bits = new DecoderRes
      })
    }
  }

  object DecodeAsidePorts{
    class DecodeAsideIn extends Bundle {
      val regLeft = UInt(32.W)
      val regRight = UInt(32.W)
    }
    class DecodeAsideOut extends Bundle {
      val reg_1 = new ReadCtrl
      val reg_2 = new ReadCtrl
    }
    class DecodeAsideIo extends Bundle {
      val in = Input(new DecodeAsideIn)
      val out = Output(new DecodeAsideOut)
    }
  }
  object DecodePipePorts {
    class DecodeSrcInfo extends Bundle {
      val pc = UInt(pcWidth)
      val inst = UInt(instWidth)
      // For Branch Prediction
      val predictTaken = Bool()
    }
    class DecodePipeOut extends Bundle {
      val bits = new ExeSrcInfo
      val readInfo = new Bundle() {
        val reg_1 = new ReadCtrl
        val reg_2 = new ReadCtrl
      }
      val fetchInfo = new DecodeSrcInfo
      val req = Bool()
    }
    class DecodePipeIo extends Bundle {
      val fetch = new Bundle {
        val in = Input(new FetchPipeOut)
        val out = Output(new Bundle() {
          val ack = Bool()
        })
      }
      val exe = new Bundle {
        val in = Input(new Bundle {
          val ack = Bool()
        })
        val out = Output(new DecodePipeOut)
      }
      val br = Input(new ExeBranchInfo)
    }
  }
  class DecodeIo extends Bundle{
    val pipe = new DecodePipePorts.DecodePipeIo
    val aside = new DecodeAsidePorts.DecodeAsideIo
  }
}
