import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import ultra.helper._
class FuncSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Func" should "Pass" in {
    test (new Sqrt()) { core =>
      step(1)
      core.io.in.poke(403.U)
      core.io.start.poke(true.B)
      step(1)
      core.io.start.poke(false.B)
      step(30)
    }
  }
}
