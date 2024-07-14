import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import elaborate.UltraBusElaborate
class UltraBusSpec extends AnyFlatSpec with ChiselScalatestTester {
  "UltraBus" should "Pass" in {
    test (new UltraBusElaborate) { core =>
      core.io.data.in.wreq.poke(true.B)
      core.io.data.in.wdata.poke('a'.U)
      core.io.data.in.addr.poke("hbfd0_03f8".U)
      step()
      core.io.data.in.wreq.poke(false.B)
      step()
      core.io.data.in.rreq.poke(true.B)
      core.io.data.in.addr.poke("h8000_0000".U)
      core.io.data.in.rtype.poke(1.U)
      step()
      core.io.data.in.rreq.poke(false.B)
      step(100)

    }
  }
}
