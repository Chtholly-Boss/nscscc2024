package sram

import chisel3._
import SramParam._

class SramSim extends Module {
  val io = IO(new Bundle {
    val in = Input(new SramRequest)
    val out = Output(new SramResponse)
  })
  val mem = Mem(depth,UInt(wordLength.W))
  val initOut = Wire(new SramResponse)
  initOut.rData := 0.U
  io.out := initOut
  when (io.in.ce === lowLevel) {
    when (io.in.we === lowLevel) {
      mem.write(io.in.addr,io.in.wData)
    } .otherwise {
      io.out.rData := mem.read(io.in.addr)
    }
  }
}

object SramSim {
  val sramInit = Wire(new SramRequest)
  sramInit.ce := highLevel
  sramInit.oe := highLevel
  sramInit.we := highLevel
  sramInit.byteSelN := "b1111".U
  sramInit.addr := 0.U
  sramInit.wData := 0.U

  def sramReadWord(addr: UInt) : SramRequest = {
    val sramReadWord = Wire(new SramRequest)
    sramReadWord.ce := lowLevel
    sramReadWord.oe := lowLevel
    sramReadWord.we := highLevel
    sramReadWord.byteSelN := "b0000".U
    sramReadWord.addr := addr
    sramReadWord.wData := 0.U
    sramReadWord
  }

  def sramWrite(addr:UInt,data:UInt) : SramRequest = {
    val sramWriteWord = Wire(new SramRequest)
    sramWriteWord.ce := lowLevel
    sramWriteWord.oe := lowLevel
    sramWriteWord.we := highLevel
    sramWriteWord.byteSelN := "b0000".U
    sramWriteWord.addr := addr
    sramWriteWord.wData := 0.U
    sramWriteWord
  }
}