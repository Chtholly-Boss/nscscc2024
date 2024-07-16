package ultra.pipeline.exe
import chisel3._
import ultra.pipeline.regfile.RegfileUtils._
import UltraExePorts._
import ultra.bus.UltraBusUtils._
object UltraExeUtils {
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
    init
  }
  def initExeAsideIn= {
    initDataRspns
  }
  def initExeAsideOut = {
    initDataReq
  }
  def initExeBranchInfo:ExeBranchInfo = {
    val init = Wire(new ExeBranchInfo)
    init.isMispredict := false.B
    init.npc := 0.U
    init
  }
}
