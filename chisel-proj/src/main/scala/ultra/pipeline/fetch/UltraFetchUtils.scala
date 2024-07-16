package ultra.pipeline.fetch
import chisel3._
import UltraFetchPorts.FetchAsidePorts._
import UltraFetchPorts.FetchPipePorts._
import ultra.pipeline.decode.UltraDecodeUtils._
object UltraFetchUtils {
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
    def fetchInst(pc:UInt):FetchAsideOut = {
      val info = Wire(new FetchAsideOut)
      info.rreq := true.B
      info.pc := pc
      info
    }
  }
  object FetchPipeUtils {
    def initFetchOut():FetchPipeOut = {
      val init = Wire(new FetchPipeOut)
      init.req := false.B
      init.bits := initDecodeSrcInfo
      init
    }
  }
}
