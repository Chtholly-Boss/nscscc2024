package pipeline.exe
import chisel3._
import ExeParams._
import ExePorts._
import pipeline.regfile.RegfileUtils.wInit
object ExeUtils {
  val init = Wire(new ExeOut)
  init.ack := false.B
  init.bits := wInit
  init.req := false.B

  val opInit = Wire(new ExeOp)
  opInit.opFunc := 0.U
  opInit.opType := 0.U
}