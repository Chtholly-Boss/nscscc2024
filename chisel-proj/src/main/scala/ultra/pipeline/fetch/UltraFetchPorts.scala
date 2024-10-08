package ultra.pipeline.fetch
import chisel3._
import ultra.pipeline.decode.UltraDecodePorts.DecodePipePorts._
import ultra.pipeline.exe.UltraExePorts.ExePipePorts._
object UltraFetchPorts {
  object FetchAsidePorts {
    class UltraFetchAsideIn extends Bundle {
      val inst = UInt(32.W)
      val npc = UInt(32.W)
      val predictTaken = Bool()
      val isHit = Bool()
    }
    class UltraFetchAsideOut extends Bundle {
      val pc = UInt(32.W)
      val rreq = Bool()
    }
    class FetchAsideIn extends Bundle {
      val inst = UInt(32.W)
      val rvalid = Bool()
      val isInBuf = Bool()
      val pcOffset = UInt(32.W)
      val predictTaken = Bool()
    }
    class FetchAsideOut extends Bundle {
      val pc = UInt(32.W)
      val rreq = Bool()
    }
    class FetchAsideMasterIo extends Bundle{
      val out = Output(new FetchAsideOut)
      val in = Input(new FetchAsideIn)
    }
    class FetchAsideSlaveIo extends Bundle{
      val in = Input(new FetchAsideOut)
      val out = Output(new FetchAsideIn)
    }
    class UltraFetchAsideMasterIo extends Bundle {
      val out = Output(new UltraFetchAsideOut)
      val in = Input(new UltraFetchAsideIn)
    }
    class UltraFetchAsideSlaveIo extends Bundle{
      val in = Input(new UltraFetchAsideOut)
      val out = Output(new UltraFetchAsideIn)
    }
  }
  object FetchPipePorts{
    class FetchPipeOut extends Bundle {
      val req = Bool()
      val bits = new DecodeSrcInfo
    }
    class FetchPipeIo extends Bundle{
      val in = Input(new Bundle {
        val ack = Bool()
      })
      val out = Output(new FetchPipeOut)
      val br = Input(new ExeBranchInfo)
    }
  }
  class FetchIo extends Bundle {
    val pipe = new FetchPipePorts.FetchPipeIo
    val aside = new FetchAsidePorts.FetchAsideMasterIo
  }
  class UltraFetchIo extends Bundle {
    val pipe = new FetchPipePorts.FetchPipeIo
    val aside = new FetchAsidePorts.UltraFetchAsideMasterIo
  }
}
