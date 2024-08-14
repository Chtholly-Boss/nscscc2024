import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import ultra.helper._
class DoubleSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Double" should "Pass" in {
    test (new Power()) { core =>
      step(1)
      core.io.in.opLeft.poke(2.U)
      core.io.in.opRight.poke(3.U)
      core.io.in.start.poke(true.B)
      step(1)
      core.io.in.start.poke(false.B)
      step(30)

      core.io.in.opLeft.poke(4.U)
      core.io.in.opRight.poke(4.U)
      core.io.in.start.poke(true.B)
      step(1)
      core.io.in.start.poke(false.B)
      step(30)

      core.io.in.opLeft.poke(10.U)
      core.io.in.opRight.poke(3.U)
      core.io.in.start.poke(true.B)
      step(1)
      core.io.in.start.poke(false.B)
      step(30)
    }
  }
}
