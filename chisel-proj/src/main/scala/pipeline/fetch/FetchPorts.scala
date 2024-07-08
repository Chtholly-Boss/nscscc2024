package pipeline.fetch
import chisel3._
import pipeline.decode.DecodePorts.DecodeSrcInfo
object FetchPorts {
  class FetchOut extends Bundle {
    val req = Bool()
    val bits = new DecodeSrcInfo
  }
  class FetchAsideOut extends Bundle {
    val req = Bool()
    val pc = UInt(32.W)
  }
  // This may be replaced by Bus Bundle
  class FetchAsideIn extends Bundle {
    val inst = UInt(32.W)
    val rrdy = Bool()
    val rvalid = Bool()
  }
  class FetchAsideIo extends Bundle {
    val in = Input(new FetchAsideIn)
    val out = Output(new FetchAsideOut)
  }
  class FetchIo extends Bundle {
    val in = Input(new Bundle() {
      val ack = Bool()
    })
    val out = Output(new FetchOut)
    val aside = new FetchAsideIo
  }
}