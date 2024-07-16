package ultra.caches.bpbuffer

import chisel3._
import ultra.caches.icache.IcacheParams._
import BpBufferPorts._
import BpBufferUtils._
import ultra.bus.UltraBusPorts._
import ultra.helper.MultiMux1

class BpBuffer extends Module {
  val io = IO(new BpBufferIo)
  io.core.out := initBpOut()
  val bpers = Seq.fill(bandwidth/32)(Module(new BpUnit))
  bpers.foreach( bper => {
    bper.io.in.pc := io.core.in.pc
    bper.io.in.index := bpers.indexOf(bper).U
    bper.io.in.rvalid := io.icache.in.rvalid
    bper.io.in.inst := io.icache.in.rdata(
      (bpers.indexOf(bper) + 1) * 32 - 1,bpers.indexOf(bper) * 32
    )
  })
  val bpMux = Module(new MultiMux1(bpers.length,new BpOut,0.U.asTypeOf(new BpOut)))
  bpMux.io.inputs.zip(bpers).foreach{
    case (dst,src) =>
      dst.valid := src.io.out.isMatched
      dst.bits := src.io.out.bits
  }
  io.core.out := bpMux.io.output.bits
  bpMux.io.output.valid := DontCare

}
