import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import ultra.helper._
class SingleSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Single" should "Pass" in {
    test (new Sqrt()) { core =>
      step(1)
      core.io.in.poke(200.U)  // put your test case here
      core.io.start.poke(true.B)
      step(1)
      core.io.start.poke(false.B)
      step(30)

      core.io.in.poke(400.U)  // put your test case here
      core.io.start.poke(true.B)
      step(1)
      core.io.start.poke(false.B)
      step(30)

      core.io.in.poke(413.U)  // put your test case here
      core.io.start.poke(true.B)
      step(1)
      core.io.start.poke(false.B)
      step(30)
    }
  }
}
