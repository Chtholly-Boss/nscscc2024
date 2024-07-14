import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import elaborate.Soc
class SocSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Fetch2Decode2Exe" should "Pass" in {
    test (new Soc()) { core =>
      core.clock.setTimeout(0)
      step(80000)
    }
  }
}
