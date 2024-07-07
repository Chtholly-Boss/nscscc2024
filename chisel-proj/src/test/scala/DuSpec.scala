import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import pipeline.decode.Du
import pipeline.decode.DecodeParam._
class DuSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Du" should "pass" in {
    test(new Du()) { core =>
      core.clock.setTimeout(0)
      // ori     $r1,$r0,0x2
      core.io.in.inst.poke("h03800801".U)
      step()
      // mul.w     $r3,$r1,$r2
      core.io.in.inst.poke("h001c0823".U)
      step()
    }
  }
}
