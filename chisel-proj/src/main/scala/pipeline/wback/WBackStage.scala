package pipeline.wback

import chisel3._
import chisel3.util._
import WBackPorts._
import WBackUtils._
import pipeline.regfile.RegfileUtils.{wInit => RegWriteInit}

class WBackStage extends Module {
  val io = IO(new WBackIo)
  val outReg: WBackOut = RegInit(init)
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
        outReg.bits := io.in.bits
        outReg.ack := true.B

      } .otherwise {
        outReg.bits := RegWriteInit
        outReg.ack := false.B
      }
    }
    is (WRITE) {
      stat := IDLE
      outReg:= init
    }
  }
}