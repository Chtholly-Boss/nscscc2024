package pipeline.exe
import chisel3._
import chisel3.util._
import ExePorts._
import ExeUtils._

class ExeStage extends Module {
  val io = IO(new ExeIo)
  val outReg: ExeOut = RegInit(init)
  io.out := outReg

  object State extends ChiselEnum {
    val IDLE,ALUEXE,DONE = Value
  }
  import State._
  val stat = RegInit(IDLE)
  switch(stat) {
    is (IDLE) {

    }
    is (ALUEXE) {

    }
    is (DONE) {

    }
  }
}