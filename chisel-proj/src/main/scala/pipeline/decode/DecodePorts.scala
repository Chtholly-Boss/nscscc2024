package pipeline.decode

import chisel3._
import DecodeParam._
import pipeline.regfile.RegfilePorts._
import pipeline.exe.ExePorts.ExeOp

object DecodePorts {
  class DecodeSrcInfo extends Bundle {
    val pc = UInt(pcWidth)
    val inst = UInt(instWidth)
  }
  class DecodeReq extends Bundle {
    val req = Bool()
    val bits = new DecodeSrcInfo
  }
  class DecodeRes extends Bundle {
    val exeOp = new ExeOp
    val hasImm = Bool()
    val imm = UInt(immWidth)
    val reg_1 = new ReadCtrl
    val reg_2 = new ReadCtrl
    val wCtrl = new WriteCtrl
  }
  class DecodeOut extends Bundle {
    val ack = Bool()
    val bits = new DecodeRes
    val req = Bool()
  }
  class DecoderIo extends Bundle {
    val in = Input(new DecodeSrcInfo)
    val out = Output(new Bundle {
      val isMatched = Bool()
      val bits = new DecodeRes
    })
  }
  class DecodeIo extends Bundle {
    val in = Input(new DecodeReq)
    val out = Output(new DecodeOut)
}
}