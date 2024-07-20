package ultra.caches.blkmem

import chisel3._
import chisel3.util._
import BlockMemPorts._

class BlockMem(depth:Int,width:Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(new BlockMemIn(log2Ceil(depth),width))
    val out = Output(new BlockMemOut(width))
  })
  val regs = RegInit(VecInit(Seq.fill(depth)(0.U(width.W))))
  val outReg = RegInit(0.U(width.W))
  io.out.rdata := outReg
  when (io.in.wen) {
    regs(io.in.addr) := io.in.wdata
    outReg := io.in.wdata
  }.otherwise{
    outReg := regs(io.in.addr)
  }

}
