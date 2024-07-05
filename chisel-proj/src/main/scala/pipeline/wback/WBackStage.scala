package pipeline.wback

import chisel3._
import chisel3.util._
import WBackPorts._
import WBackUtils._
import pipeline.regfile.RegfileUtils.{wInit => RegWriteInit}

class WBackStage extends Module {
  val io = IO(new WBackIo)
  val outReg = RegInit(new WBackOut)
  outReg := init
  io.out := outReg

  object State extends ChiselEnum {
    val IDLE,WRITE = Value
  }
  import State._
  val stat = RegInit(IDLE)

  switch(stat) {
    is (IDLE) {
      when (io.in.req) {
        stat := WRITE
        outReg := io.in.bits
      } .otherwise {
        outReg := RegWriteInit
      }
    }
    is (WRITE) {
      stat := IDLE
      outReg := RegWriteInit
    }
  }
}