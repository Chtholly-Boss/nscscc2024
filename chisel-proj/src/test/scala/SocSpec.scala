import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SocSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Soc" should "Pass" in {
    test (new SoC()) { core =>
      core.clock.setTimeout(0)
      step(2000)
    }
  }
}
