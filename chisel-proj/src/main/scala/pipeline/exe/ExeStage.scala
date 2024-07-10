package pipeline.exe
import chisel3._
import chisel3.util._
import ExePorts._
import ExeUtils._

class ExeStage extends Module {
  val io = IO(new ExeIo)
  val outReg: ExeOut = RegInit(initExeOut)
  io.out := outReg

  val alu = Module(new Alu)
  val opLeft = RegInit(0.U(32.W))
  val opRight = RegInit(0.U(32.W))
  val exeOp = RegInit(initOp)
  // Ports may need to be revised
  alu.io.in.op := exeOp
  alu.io.in.operand.left := opLeft
  alu.io.in.operand.right := opRight

  object State extends ChiselEnum {
    val IDLE,ALUEXE,DONE = Value
  }
  import State._
  val stat = RegInit(IDLE)
  switch(stat) {
    is (IDLE) {
      when (io.in.decode.req) {
        stat := ALUEXE
        exeOp := io.in.decode.bits.exeOp

        //outReg := true.B
        outReg.bits.en := io.in.decode.bits.wCtrl.en
        outReg.bits.addr := io.in.decode.bits.wCtrl.addr
      } .otherwise {
        //outReg := init
      }
    }
    is (ALUEXE) {
      stat := DONE
      outReg.req := true.B
      outReg.bits.data := alu.io.out.res
      //outReg.ack := false.B
    }
    is (DONE) {
      when (io.in.ack) {
        stat := IDLE
        //outReg := init
      }
    }
  }
}