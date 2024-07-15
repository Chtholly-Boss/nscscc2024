package caches.bpbuffer
import chisel3._
import chisel3.util._
import pipeline.decode.DecodeParam.{_2RI16 => Inst}
import BpBufferPorts._
import BpBufferUtils._
import caches.icache.IcacheParams._
class BpUnit extends Module {
  val io = IO(new BpUnitIo)
  io.out.bits := initBpOut()
  io.out.isMatched := io.in.pc(offsetWidth-1,2) === io.in.index
  when(io.in.rvalid){
    // BTFNT
    switch(io.in.inst(31,26)){
      is(Inst.beq,Inst.bge,Inst.bne){
        when(io.in.inst(25) === 1.U){
          io.out.bits.predictTaken := true.B
          io.out.bits.offset := (io.in.inst(25,10) ## 0.U(2.W)).asSInt.asUInt
        }
      }
      is(Inst.bl,Inst.b_){
        io.out.bits.predictTaken := true.B
        io.out.bits.offset := (
          io.in.inst(9,0) ##
          io.in.inst(25,10) ##
          0.U(2.W)
          ).asSInt.asUInt
      }
    }
  }
}