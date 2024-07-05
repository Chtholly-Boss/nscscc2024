package pipeline.decode
import chisel3._
import chisel3.util._
import DecodePorts._
import DecodeUtils._

class DecodeStage extends Module {
  val io = IO(new DecodeIo)
  val outReg:DecodeOut = RegInit(init)
  io.out := outReg

  object State extends ChiselEnum {
    val IDLE,DECODE,DONE = Value
  }
  import State._
  val stat = RegInit(IDLE)
  switch(stat) {
    is (IDLE) {

    }
    is (DECODE) {

    }
    is (DONE) {

    }
  }
}