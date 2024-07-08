import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import elaborate.FetchElaborate
class FetchSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Fetch" should "Pass" in {
    test (new FetchElaborate()) { core =>
      core.clock.setTimeout(0)
      core.io.in.ack.poke(true.B)
      step(50)
    }
  }
}
