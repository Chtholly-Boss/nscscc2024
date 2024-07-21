package ultra.pipeline.exe
import chisel3._
import chisel3.util._
import UltraExePorts.AluPorts.AluIo
import UltraExeParams._
import UltraExeParams.{ExeType => tp}
import UltraExeParams.{Arithmetic => arith,Logic => lg,Shift => sht}
class UltraAlu extends Module {
  val io = IO(new AluIo)
  // alias the Port signals
  val left = WireDefault(io.in.operand.left)
  val right = WireDefault(io.in.operand.right)
  val res = WireDefault(0.U(32.W))
  io.out.res := res
  switch (io.in.op.opType) {
    is (tp.nop) {
      res := 0.U
    }
    is (tp.arith) {
      switch (io.in.op.opFunc) {
        is (arith.add) {
          res := (left.asSInt + right.asSInt).asUInt
        }
        is (arith.sltu) {
          res := 0.U(31.W) ## (left < right).asUInt
        }
        is (arith.sub) {
          res := (left.asSInt - right.asSInt).asUInt
        }
      }
    }
    is (tp.logic) {
      switch (io.in.op.opFunc) {
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
        is (sht.sll) {
          res := left << right(4,0)
        }
        is (sht.srl) {
          res := left >> right(4,0)
        }
      }
    }
  }
}
