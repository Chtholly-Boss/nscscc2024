package ultra.pipeline.fetch
import chisel3._
import UltraFetchPorts.FetchAsidePorts._
object UltraFetchUtils {
  object FetchState extends ChiselEnum {
    val
      RST,  // rst to init pc
      WAIT, // Wait for buffer refill
      READY // wait for ack
    = Value
  }
  object FetchAsideUtils {
    def initFetchAsideIn(): FetchAsideIn = {
      val init = Wire(new FetchAsideIn)
      init.rvalid := false.B
      init.isInBuf := false.B
      init.inst := 0.U
      init.pcOffset := 4.U
      init.predictTaken := false.B
      init
    }
    def initFetchAsideOut():FetchAsideOut = {
      val init = Wire(new FetchAsideOut)
      init.pc := 0.U
      init.rreq := false.B
      init
    }
  }
}
