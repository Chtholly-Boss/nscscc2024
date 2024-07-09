package pipeline.exe
import chisel3._
import ExeParams._
import ExePorts._
import pipeline.regfile.RegfileUtils.initWctrl
object ExeUtils {
  def initOperands:Operands = {
    val res = Wire(new Operands)
    res.left := 0.U
    res.right := 0.U
    res
  }
  def initOp:ExeOp = {
    val res = Wire(new ExeOp)
    res.opFunc := 0.U
    res.opType := 0.U
    res
  }
  def initExeSrcInfo:ExeSrcInfo = {
    val res = Wire(new ExeSrcInfo)
    res.exeOp := initOp
    res.operands := initOperands
    res.wCtrl := initWctrl
    res
  }
  def initExeOut:ExeOut = {
    val res = Wire(new ExeOut)
    res.bits := initWctrl
    res.req := false.B
    res
  }



}