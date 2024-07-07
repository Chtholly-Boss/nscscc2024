package pipeline.decode
import chisel3._
import pipeline.regfile.RegfileUtils._
import pipeline.exe.ExeUtils.opInit
import DecodePorts._

object DecodeUtils {
  def defaultRes:DecodeRes = {
    val res = Wire(new DecodeRes)
    res.reg_1 := rInit
    res.reg_2 := rInit
    res.wCtrl := wInit
    res.hasImm := 0.U
    res.imm := 0.U
    res.exeOp := opInit
    res
  }

  val init = Wire(new DecodeOut)
  init.ack := false.B
  init.req := false.B
  init.bits := defaultRes
}