import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import ultra.pipeline.exe.DivU
class DivSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Div" should "Pass" in {
    test (new DivU()) { core =>
      step(1)
      core.io.in.valid.poke(true.B)
      core.io.in.bits.left.poke(40.U)
      core.io.in.bits.right.poke(4.U)
      step(1)
      core.io.in.valid.poke(false.B)
      step(30)
    }
  }
}
