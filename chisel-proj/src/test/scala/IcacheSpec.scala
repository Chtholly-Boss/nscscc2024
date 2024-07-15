import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import elaborate._
class IcacheSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Icache" should "Pass" in {
    test (new IcacheElaborate()) { core =>
      core.clock.setTimeout(0)
      for(i <- 0 until(16)){
        core.io_pc.poke((i*4).U)
        core.io.in.rreq.poke(true.B)
        core.io.in.pc.poke((i*4).U)
        step(1)
        core.io.in.rreq.poke(false.B)
        step(20)
      }
    }
  }
}
