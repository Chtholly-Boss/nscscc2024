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

  def opInit:ExeOp = {
    val res = Wire(new ExeOp)
    res.opFunc := 0.U
    res.opType := 0.U
    res
  }

}