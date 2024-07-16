package pipeline.fetch
import chisel3._
import pipeline.decode.DecodePorts.DecodeSrcInfo
import pipeline.exe.ExePorts.ExeBranchInfo
import ultra.bus.UltraBusPorts._
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
  class FetchAsideIn(dataWidth:Int = 32) extends Bundle {
    val inst = UInt(dataWidth.W)
    val rrdy = Bool()
    val rvalid = Bool()
  }
  class FetchAsideIo(dataWidth:Int = 32) extends Bundle {
    val in = Input(new FetchAsideIn(dataWidth))
    val out = Output(new FetchAsideOut)
  }
  class FetchIo(dataWidth:Int = 32) extends Bundle {
    val in = Input(new Bundle() {
      val ack = Bool()
    })
    val out = Output(new FetchOut)
    val aside = new FetchAsideIo(dataWidth)
    val bCtrl = Input(new ExeBranchInfo)
  }
  class UltraFetchIo extends Bundle {
    val in = Input(new Bundle() {
      val ack = Bool()
    })
    val out = Output(new FetchOut)
    val aside = new InstMasterIo
    val bCtrl = Input(new ExeBranchInfo)
  }
}