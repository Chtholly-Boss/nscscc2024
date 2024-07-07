import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pipeline.exe.Alu
import pipeline.exe.ExeParams.{ExeType => tp}
import pipeline.exe.ExeParams.{Arithmetic => arith,Logic => lg,Shift => sht}

class AluSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Alu" should "pass" in {
    test(new Alu()) { core =>
      core.clock.setTimeout(0)
      // Test Arithmetic
      core.io.in.op.opType.poke(tp.arith)
      core.io.in.operand.left.poke(1.U)
      core.io.in.operand.right.poke(2.U)
      for (i <- 0 until(arith.num)) {
        core.io.in.op.opFunc.poke(i.U)
        step()
      }
      // Test Logic
      core.io.in.op.opType.poke(tp.logic)
      core.io.in.operand.left.poke("h0000_000F".U)
      core.io.in.operand.right.poke("h0000_000a".U)
      for (i <- 0 until(lg.num)) {
        core.io.in.op.opFunc.poke(i.U)
        step()
      }
      // Test Shift
      core.io.in.op.opType.poke(tp.shift)
      core.io.in.operand.left.poke("h0000_8000".U)
      core.io.in.operand.right.poke("h0000_0002".U)
      for (i <- 0 until(sht.num)) {
        core.io.in.op.opFunc.poke(i.U)
        step()
      }
    }
  }
}