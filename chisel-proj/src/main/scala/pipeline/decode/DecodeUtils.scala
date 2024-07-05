package pipeline.decode
import chisel3._
import pipeline.regfile.RegfileUtils._
import pipeline.exe.ExeUtils.opInit
import DecodePorts._

object DecodeUtils {
  val init = Wire(new DecodeOut)
  init.ack := false.B
  init.req := false.B
  init.bits.reg_1 := rInit
  init.bits.reg_2 := rInit
  init.bits.wCtrl := wInit
  init.bits.hasImm := 0.U
  init.bits.imm := 0.U
  init.bits.exeOp := opInit
}