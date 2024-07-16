package ultra.pipeline.decode.decoders

import chisel3._
import chisel3.util._
import pipeline.decode.DecodeParam.{_2RI12 => Inst}
import pipeline.exe.ExeParams._
class Decoder_2RI12 extends BaseDecoder {
  val opcode = WireDefault(io.in.inst(31,22))
  val imm12 = WireDefault(io.in.inst(21,10))
  val rj = WireDefault(io.in.inst(9,5))
  val rd = WireDefault(io.in.inst(4,0))
  // - Extend Immediates
  val immSext = Wire(SInt(dataWidth))
  val immZext = Wire(UInt(dataWidth))
  immSext := imm12.asSInt
  immZext := imm12
  // - Default Assignment
  io.out.bits.hasImm := true.B
  io.out.bits.wCtrl.en := true.B
  io.out.bits.wCtrl.addr:= rd
  io.out.bits.reg_1.addr := rj
  // - decode opcode
  switch(opcode) {
    is (Inst.addi_w) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.arith
      io.out.bits.exeOp.opFunc := Arithmetic.add
      io.out.bits.imm := immSext.asUInt
    }
    is (Inst.sltui) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.arith
      io.out.bits.exeOp.opFunc := Arithmetic.sltu
      io.out.bits.imm := immSext.asUInt
    }
    is (Inst.andi) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.logic
      io.out.bits.exeOp.opFunc := Logic.and
      io.out.bits.imm := immZext.asUInt
    }
    is (Inst.ori) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.logic
      io.out.bits.exeOp.opFunc := Logic.or
      io.out.bits.imm := immZext.asUInt
    }
    is (Inst.ld_b) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.load
      io.out.bits.exeOp.opFunc := Load.ld_b
      io.out.bits.imm := immSext.asUInt
    }
    is (Inst.ld_w) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.load
      io.out.bits.exeOp.opFunc := Load.ld_w
      io.out.bits.imm := immSext.asUInt
    }
    is (Inst.st_b) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.store
      io.out.bits.exeOp.opFunc := Store.st_b
      io.out.bits.imm := immSext.asUInt
      io.out.bits.wCtrl.en := false.B
      io.out.bits.reg_2.addr := rd
    }
    is (Inst.st_w) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.store
      io.out.bits.exeOp.opFunc := Store.st_w
      io.out.bits.imm := immSext.asUInt
      io.out.bits.wCtrl.en := false.B
      io.out.bits.reg_2.addr := rd
    }
  }
}
