import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import elaborate.Uart
class UartSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Uart" should "Pass" in {
    test (new Uart) { core =>
      core.io.in.start.poke(true.B)
      core.io.in.data.poke('a'.U)
      step(100)
    }
  }
}
