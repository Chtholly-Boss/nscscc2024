import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import elaborate.F2D2E
class SocSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Fetch2Decode2Exe" should "Pass" in {
    test (new F2D2E()) { core =>
      core.clock.setTimeout(0)
      core.io.in.ack.poke(true.B)
      core.io.in.exeAside.rrdy.poke(true.B)
      core.io.in.exeAside.wdone.poke(true.B)

      core.io.in.exeAside.rdata.poke(0.U)
      core.io.in.exeAside.rvalid.poke(true.B)
      core.io.in.exeAside.wrdy.poke(true.B)
      step(50)
    }
  }
}
