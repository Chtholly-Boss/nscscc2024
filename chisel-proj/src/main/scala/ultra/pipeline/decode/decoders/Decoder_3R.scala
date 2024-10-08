package ultra.pipeline.decode.decoders

import chisel3._
import chisel3.util._
import ultra.pipeline.decode.UltraDecodeParams.{_3R => Inst}
import ultra.pipeline.exe.UltraExeParams._
class Decoder_3R extends BaseDecoder {
  // alias
  val opType = io.out.bits.exeOp.opType
  val opFunc = io.out.bits.exeOp.opFunc

  val opcode      = WireDefault(io.in.inst(31, 15))
  val rk          = WireDefault(io.in.inst(14, 10))
  val rj          = WireDefault(io.in.inst(9, 5))
  val rd          = WireDefault(io.in.inst(4, 0))
  val imm5 = WireDefault(io.in.inst(14,10))

  io.out.bits.wCtrl.en := true.B
  io.out.bits.wCtrl.addr := rd
  io.out.bits.reg_1.addr := rj
  io.out.bits.reg_2.addr := rk

  switch (opcode) {
    is (Inst.add_w) {
      io.out.isMatched := true.B
      opType := ExeType.arith
      opFunc := Arithmetic.add
    }
    is (Inst.sub_w) {
      io.out.isMatched := true.B
      opType := ExeType.arith
      opFunc := Arithmetic.sub
    }
    is (Inst.mul_w) {
      io.out.isMatched := true.B
      opType := ExeType.arith
      opFunc := Arithmetic.mul
    }
    is (Inst.and_w) {
      io.out.isMatched := true.B
      opType := ExeType.logic
      opFunc := Logic.and
    }
    is (Inst.or_w) {
      io.out.isMatched := true.B
      opType := ExeType.logic
      opFunc := Logic.or
    }
    is (Inst.xor_w) {
      io.out.isMatched := true.B
      opType := ExeType.logic
      opFunc := Logic.xor
    }
    is (Inst.sll_w) {
      io.out.isMatched := true.B
      opType := ExeType.shift
      opFunc := Shift.sll
    }
    is (Inst.srl_w) {
      io.out.isMatched := true.B
      opType := ExeType.shift
      opFunc := Shift.srl
    }
    is (Inst.slli_w) {
      io.out.isMatched := true.B
      opType := ExeType.shift
      opFunc := Shift.sll
      io.out.bits.hasImm := true.B
      io.out.bits.imm := imm5
    }
    is (Inst.srli_w) {
      io.out.isMatched := true.B
      opType := ExeType.shift
      opFunc := Shift.srl
      io.out.bits.hasImm := true.B
      io.out.bits.imm := imm5
    }
    is (Inst.div_w){
      io.out.isMatched := true.B
      opType := ExeType.arith
      opFunc := Arithmetic.div
    }
    is (Inst.mod_w){
      io.out.isMatched := true.B
      opType := ExeType.arith
      opFunc := Arithmetic.mod
    }
  }
}
