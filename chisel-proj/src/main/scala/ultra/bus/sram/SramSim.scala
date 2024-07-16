package ultra.bus.sram

import chisel3._
import SramParam._
import SramPorts._
class SramSim extends Module {
  val io = IO(new SramIO)
  val mem = Mem(depth,UInt(wordLength.W))
  io.out.rData := 0.U
  when (io.in.ce === lowLevel) {
    when (io.in.we === lowLevel) {
      mem.write(io.in.addr,io.in.wData)
    } .otherwise {
      when (io.in.oe === lowLevel) {
        io.out.rData := mem.read(io.in.addr)
      }
    }
  }
}