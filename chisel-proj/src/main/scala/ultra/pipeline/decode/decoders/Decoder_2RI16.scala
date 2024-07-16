package ultra.pipeline.decode.decoders

import chisel3._
import chisel3.util._
import pipeline.decode.DecodeParam.{_2RI16 => Inst}
import pipeline.exe.ExeParams._
class Decoder_2RI16 extends BaseDecoder {
  val opcode = WireDefault(io.in.inst(31,26))
  val rj = WireDefault(io.in.inst(9,5))
  val rd = WireDefault(io.in.inst(4,0))
  val imm16 = WireDefault(io.in.inst(25,10))
  val imm26 = WireDefault(rj ## rd ## io.in.inst(25,10))
  val immSext = WireDefault(0.S(dataWidth))

  io.out.bits.hasImm := true.B
  io.out.bits.imm := immSext.asUInt
  io.out.bits.reg_1.addr := rj
  io.out.bits.reg_2.addr := rd
  switch (opcode) {
    is (Inst.jirl) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.jump
      io.out.bits.exeOp.opFunc := JumpBranch.jirl
      immSext := imm16.asSInt << 2
      io.out.bits.wCtrl.en := true.B
      io.out.bits.wCtrl.addr := rd
      io.out.bits.wCtrl.data := io.in.pc + 4.U
    }
    is (Inst.beq) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.branch
      io.out.bits.exeOp.opFunc := Branch.beq
      immSext := imm16.asSInt << 2
    }
    is (Inst.bge) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.branch
      io.out.bits.exeOp.opFunc := Branch.bge
      immSext := imm16.asSInt << 2
    }
    is (Inst.bne) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.branch
      io.out.bits.exeOp.opFunc := Branch.bne
      immSext := imm16.asSInt << 2
    }
    is (Inst.bl) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.branch
      io.out.bits.exeOp.opFunc := Branch.bl
      io.out.bits.wCtrl.en := true.B
      io.out.bits.wCtrl.addr := 1.U
      io.out.bits.wCtrl.data := io.in.pc + 4.U
      immSext := imm26.asSInt << 2
    }
    is (Inst.b_) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.branch
      io.out.bits.exeOp.opFunc := Branch.b_
      immSext := imm26.asSInt << 2
    }
  }
}
