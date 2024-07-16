package ultra.pipeline.decode.decoders

import chisel3._
import chisel3.util._
import ultra.pipeline.decode.UltraDecodeParams.{_special => Inst}
import ultra.pipeline.exe.UltraExeParams._
class Decoder_Special extends BaseDecoder {
  val opcode      = WireDefault(io.in.inst(31,25))
  val imm         = WireDefault(io.in.inst(24,5))
  val rd          = WireDefault(io.in.inst(4, 0))
  val imm12I = WireDefault(imm ## 0.U(12.W))

  io.out.bits.wCtrl.en := true.B
  io.out.bits.wCtrl.addr := rd
  // Special Case of Add
  io.out.bits.hasImm := true.B
  io.out.bits.reg_1.addr := 0.U
  switch (opcode) {
    is (Inst.lu12i_w) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.arith
      io.out.bits.exeOp.opFunc := Arithmetic.add
      io.out.bits.imm := imm12I
    }
    is (Inst.pcaddu12i) {
      io.out.isMatched := true.B
      io.out.bits.exeOp.opType := ExeType.arith
      io.out.bits.exeOp.opFunc := Arithmetic.add
      io.out.bits.imm := imm12I + io.in.pc
    }
  }
}
