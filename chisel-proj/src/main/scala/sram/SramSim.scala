package sram

import chisel3._
import SramParam._
import SramPorts._
class SramSim extends Module {
  val io = IO(new SramIO)
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