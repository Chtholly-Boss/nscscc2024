package pipeline.exe
import chisel3._
import ExeParams._
import ExePorts._
import pipeline.regfile.RegfileUtils.{initRctrl, initWctrl}
object ExeUtils {
  def initOperands:Operands = {
    val init = Wire(new Operands)
    init.left := 0.U
    init.right := 0.U
    init
  }
  def initOp:ExeOp = {
    val init = Wire(new ExeOp)
    init.opFunc := 0.U
    init.opType := 0.U
    init
  }
  def initExeSrcInfo:ExeSrcInfo = {
    val init = Wire(new ExeSrcInfo)
    init.exeOp := initOp
    init.operands.imm := 0.U
    init.operands.hasImm := false.B
    init.operands.regData_1 := 0.U
    init.operands.regData_2 := 0.U
    init.wCtrl := initWctrl
    init
  }
  def initExeOut:ExeOut = {
    val init = Wire(new ExeOut)
    init.bits := initWctrl
    init.req := false.B
    init
  }
  def initExeAsideIn:ExeAsideIn = {
    val init = Wire(new ExeAsideIn)
    init.rvalid := false.B
    init.rrdy := false.B
    init.rdata := 0.U
    init.wdone := false.B
    init.wrdy := false.B
    init
  }
  def initExeAsideOut:ExeAsideOut = {
    val init = Wire(new ExeAsideOut)
    init.addr := 0.U
    init.wdata := 0.U
    init.rreq := false.B
    init.wreq := false.B
    init
  }
  def initExeBranchInfo:ExeBranchInfo = {
    val init = Wire(new ExeBranchInfo)
    init.isMispredict := false.B
    init.npc := 0.U
    init
  }

}