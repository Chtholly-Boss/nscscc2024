package pipeline.exe
import chisel3._
import chisel3.util._
import ExePorts._
import ExeUtils._

class ExeStage extends Module {
  val io = IO(new ExeIo)
  io.out.ack := false.B
  io.out.exe := initExeOut

  val outReg: ExeOut = RegInit(initExeOut)
  io.out.exe := outReg
  val srcInfo = RegInit(initExeSrcInfo)
  val alu = Module(new Alu)
  alu.io.in.op := srcInfo.exeOp
  alu.io.in.operand.left := srcInfo.operands.regData_1
  when (srcInfo.operands.hasImm) {
    alu.io.in.operand.right := srcInfo.operands.imm
  } .otherwise {
    alu.io.in.operand.right := srcInfo.operands.regData_2
  }

  object State extends ChiselEnum {
    val IDLE,ALUEXE,DONE = Value
  }
  import State._
  val stat = RegInit(IDLE)
  switch(stat) {
    is (IDLE) {
      when (io.in.decode.req) {
        stat := ALUEXE
        // Cache the inputs
        srcInfo := io.in.decode.bits
        io.out.ack := true.B
      } .otherwise {
        io.out.ack := false.B
      }
    }
    is (ALUEXE) {
      stat := DONE
      outReg.req := true.B
      outReg.bits.en := srcInfo.wCtrl.en
      outReg.bits.addr := srcInfo.wCtrl.addr
      outReg.bits.data := alu.io.out.res
    }
    is (DONE) {
      when (io.in.ack) {
        stat := IDLE
        outReg := initExeOut
      }
    }
  }
}