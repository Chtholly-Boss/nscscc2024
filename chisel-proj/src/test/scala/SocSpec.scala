import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import elaborate.Fetch2Decode
class SocSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Fetch2Decode" should "Pass" in {
    test (new Fetch2Decode()) { core =>
      core.clock.setTimeout(0)
      core.io.ack.poke(true.B)
      step(50)
    }
  }
}
