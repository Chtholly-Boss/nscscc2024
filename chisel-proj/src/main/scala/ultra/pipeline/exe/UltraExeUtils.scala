package ultra.pipeline.exe
import chisel3._
import chisel3.util._
import ultra.pipeline.regfile.RegfileUtils._
import UltraExePorts.ExeAsidePorts._
import UltraExePorts.AluPorts._
import UltraExePorts.ExePipePorts._
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
    val init = Wire(new ExeAsideIn)
    init.rdata := 0.U
    init.wdone := false.B
    init.rvalid := false.B
    init.rrdy := true.B
    init.wrdy := true.B
    init
  }
  def initExeAsideOut = {
    val init = Wire(new ExeAsideOut)
    init.addr := 0.U
    init.wdata := 0.U
    init.rreq := false.B
    init.wreq := false.B
    init.byteSelN := "b0000".U
    init
  }
  def initExeBranchInfo:ExeBranchInfo = {
    val init = Wire(new ExeBranchInfo)
    init.isMispredict := false.B
    init.npc := 0.U
    init
  }
  def selByte(addr:UInt):UInt = {
    val byteSelN = WireDefault(0.U(4.W))
    switch(addr(1,0)){
      is("b00".U){
        byteSelN := "b1110".U
      }
      is("b01".U){
        byteSelN := "b1101".U
      }
      is("b10".U){
        byteSelN := "b1011".U
      }
      is("b11".U){
        byteSelN := "b0111".U
      }
    }
    byteSelN
  }
  def selByteInWords(byteSelN:UInt,data:UInt):UInt = {
    val res = WireDefault(0.U(32.W))
    switch(byteSelN){
      is("b1110".U){
        res := data(7,0)
      }
      is("b1101".U){
        res := data(15,8)
      }
      is("b1011".U){
        res := data(23,16)
      }
      is("b0111".U){
        res := data(31,24)
      }
      is("b0000".U){
        res := data
      }
    }
    res
  }
}
