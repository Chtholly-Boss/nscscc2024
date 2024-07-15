package pipeline.fetch
import chisel3._
import UltraFetchPorts._
object UltraFetchUtils {
  object FetchState extends ChiselEnum {
    val
      RST,  // rst to init pc
      WAIT, // Wait for buffer refill
      READY // wait for ack
    = Value
  }
  def initFetchAsideIn(): UltraFetchAsideIn = {
    val init = Wire(new UltraFetchAsideIn)
    init.rvalid := false.B
    init.isInBuf := false.B
    init.inst := 0.U
    init.pcOffset := 4.U
    init.predictTaken := false.B
    init
  }
  def initFetchAsideOut():UltraFetchAsideOut = {
    val init = Wire(new UltraFetchAsideOut)
    init.pc := 0.U
    init.rreq := false.B
    init
  }
}
