package caches.bpbuffer
import chisel3._
import caches.icache.IcacheParams._
import BpBufferPorts._
import BpBufferUtils._
import helper.MultiMux1
import bus.ultra.UltraBusPorts._
class BpBuffer extends Module {
  val io = IO(new Bundle() {
    val icache = new Bundle {
      val in = Input(new InstRspns)
    }
    val fetch = new Bundle {
      val in = Input(new Bundle {
        val pc = UInt(32.W)       // For Selecting the outputs
      })
      val out = Output(new BpOut)
    }
  })
  io.fetch.out := initBpOut()
  val bpers = Seq.fill(bandwidth/32)(Module(new BpUnit))
  bpers.foreach( bper => {
    bper.io.in.pc := io.fetch.in.pc
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
  io.fetch.out := bpMux.io.output.bits
  bpMux.io.output.valid := DontCare

}
