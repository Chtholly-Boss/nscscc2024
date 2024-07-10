package pipeline.exe
import chisel3._
import chisel3.util._
import ExePorts.AluIo
import ExeParams._
import ExeParams.{ExeType => tp}
import ExeParams.{Arithmetic => arith,Logic => lg,Shift => sht}
class Alu extends Module {
  val io = IO(new AluIo)
  val left = WireDefault(io.in.operand.left)
  val right = WireDefault(io.in.operand.right)
  val res = Wire(UInt(dataWidth))
  res := 0.U
  io.out.res := res
  switch (io.in.op.opType) {
    is (tp.nop) {
      res := 0.U
    }
    is (tp.arith) {
      switch (io.in.op.opFunc) {
        is (arith.nop) {
          res := 0.U
        }
        is (arith.add) {
          res := (left.asSInt + right.asSInt).asUInt
        }
        is (arith.sltu) {
          res := 0.U(31.W) ## (left < right).asUInt
        }
        is (arith.sub) {
          res := (left.asSInt - right.asSInt).asUInt
        }
        is (arith.mul) {
          res := (left.asSInt * right.asSInt)(31,0).asUInt
        }
      }
    }
    is (tp.logic) {
      switch (io.in.op.opFunc) {
        is (lg.nop) {
          res := 0.U
        }
        is (lg.or) {
          res := left | right
        }
        is (lg.and) {
          res := left & right
        }
        is (lg.xor) {
          res := left ^ right
        }
      }
    }
    is (tp.shift) {
      switch (io.in.op.opFunc) {
        is (sht.nop) {
          res := 0.U
        }
        is (sht.sll) {
          res := left << right(4,0)
        }
        is (sht.srl) {
          res := left >> right(4,0)
        }
      }
    }
    is (tp.load,tp.store) {
      res := (left.asSInt + right.asSInt).asUInt
    }
  }
}
