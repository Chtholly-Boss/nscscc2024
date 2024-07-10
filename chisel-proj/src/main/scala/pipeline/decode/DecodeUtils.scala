package pipeline.decode
import chisel3._
import pipeline.regfile.RegfileUtils._
import pipeline.exe.ExeUtils._
import DecodePorts._


object DecodeUtils {
  def initDecoderRes:DecoderRes = {
    val res = Wire(new DecoderRes)
    res.reg_1 := initRctrl
    res.reg_2 := initRctrl
    res.wCtrl := initWctrl
    res.hasImm := 0.U
    res.imm := 0.U
    res.exeOp := initOp
    res
  }
  def initDecodeAsideIn:DecodeAsideIn = {
    val res = Wire(new DecodeAsideIn)
    res.regLeft := 0.U
    res.regRight := 0.U
    res
  }
  def initDecodeAsideOut:DecodeAsideOut = {
    val res = Wire(new DecodeAsideOut)
    res.reg_1 := initRctrl
    res.reg_2 := initRctrl
    res
  }
  def initDecodeSrcInfo:DecodeSrcInfo = {
    val res = Wire(new DecodeSrcInfo)
    res.pc := 0.U
    res.inst := 0.U
    res
  }
  def initDecodeOut:DecodeOut = {
    val res = Wire(new DecodeOut)
    res.bits := initExeSrcInfo
    res.req := false.B
    res
  }
}