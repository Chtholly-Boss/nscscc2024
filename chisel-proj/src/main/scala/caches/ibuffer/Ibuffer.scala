package caches.ibuffer

import chisel3._
import IbufferPorts._
import IbufferUtils._
import IbufferParams._
import helper.MultiMux1
class Ibuffer extends Module {
  val io = IO(new IbufferIo)
  io.core.out.bits := initInstOut()
  io.core.out.hit := false.B

  val pcTag = RegInit(0.U((1+tagWidth).W))
  when(io.icache.in.rvalid){
    pcTag := 1.U(1.W) ## io.icache.in_pc(21,offsetWidth)
  }
  // Choose an instruction in the buffer
  val ibufs = Seq.fill(bandWidth/32)(Module(new IbufferUnit))
  ibufs.foreach( ibuf => {
    ibuf.io.in.pc := io.core.in.pc
    ibuf.io.in.index := ibufs.indexOf(ibuf).U
    ibuf.io.in.rvalid := io.icache.in.rvalid
    ibuf.io.in.inst := io.icache.in.rdata(
      (ibufs.indexOf(ibuf) + 1) * 32 - 1,ibufs.indexOf(ibuf) * 32
    )
  })
  val ibufMux = Module(new MultiMux1(ibufs.length,new InstOut,0.U.asTypeOf(new InstOut)))
  ibufMux.io.inputs.zip(ibufs).foreach{
    case (dst,src) =>
      dst.valid := src.io.out.isMatched
      dst.bits := src.io.out.bits
  }
  io.core.out.bits := ibufMux.io.output.bits
  io.core.out.hit := (
    pcTag(validBit) === 1.U &&
      pcTag(tagWidth-1,0) === io.core.in.pc(21,offsetWidth)
    )

  ibufMux.io.output.valid := DontCare
}
