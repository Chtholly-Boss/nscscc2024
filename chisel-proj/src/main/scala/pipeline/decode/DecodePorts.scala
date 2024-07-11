package pipeline.decode

import chisel3._
import DecodeParam._
import pipeline.regfile.RegfilePorts._
import pipeline.exe.ExePorts._
import pipeline.fetch.FetchPorts._

object DecodePorts {
  // Decoder IO
  class DecoderRes extends Bundle {
    val exeOp = new ExeOp
    val hasImm = Bool()
    val imm = UInt(immWidth)
    val reg_1 = new ReadCtrl
    val reg_2 = new ReadCtrl
    val wCtrl = new WriteCtrl
  }
  class DecoderIo extends Bundle {
    val in = Input(new DecodeSrcInfo)
    val out = Output(new Bundle {
      val isMatched = Bool()
      val bits = new DecoderRes
    })
  }

  // Decode aside Ports: to communicate with regfile
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
  // Decode IO: to cooperate in the pipeline
  class DecodeSrcInfo extends Bundle {
    val pc = UInt(pcWidth)
    val inst = UInt(instWidth)
  }
  class DecodeOut extends Bundle {
    val bits = new ExeSrcInfo
    val fetchInfo = new DecodeSrcInfo
    val req = Bool()
  }
  // Ports of DecodeStage
  class DecodeIo extends Bundle {
    val in = Input(new Bundle {
      val fetch = new FetchOut
      val ack = Bool()
    })
    val out = Output(new Bundle() {
      val decode = new DecodeOut
      val ack = Bool()
    })
    val aside = new DecodeAsideIo
    val bCtrl = Input(new ExeBranchInfo)
}
}