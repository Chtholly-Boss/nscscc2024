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
    res.operands.imm := 0.U
    res.operands.hasImm := false.B
    res.operands.regData_1 := 0.U
    res.operands.regData_2 := 0.U
    res.wCtrl := initWctrl
    res
  }
  def initExeOut:ExeOut = {
    val res = Wire(new ExeOut)
    res.bits := initWctrl
    res.req := false.B
    res
  }
  def initExeAsideIn:ExeAsideIn = {
    val res = Wire(new ExeAsideIn)
    res.rvalid := false.B
    res.rrdy := false.B
    res.rdata := 0.U
    res.wdone := false.B
    res
  }
  def initExeAsideOut:ExeAsideOut = {
    val res = Wire(new ExeAsideOut)
    res.addr := 0.U
    res.wdata := 0.U
    res.rreq := false.B
    res.wreq := false.B
    res
  }


}