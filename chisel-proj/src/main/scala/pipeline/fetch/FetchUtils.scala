package pipeline.fetch
import chisel3._
import FetchPorts._
import pipeline.decode.DecodeUtils._
object FetchUtils {
  def initFetchOut:FetchOut = {
    val init = Wire(new FetchOut)
    init.req := false.B
    init.bits := initDecodeSrcInfo
    init
  }
  def initFetchAsideIn(dataWidth:Int = 32):FetchAsideIn = {
    val init = Wire(new FetchAsideIn(dataWidth))
    init.inst := 0.U
    init.rrdy := false.B
    init.rvalid := false.B
    init
  }
  def initFetchAsideOut:FetchAsideOut = {
    val init = Wire(new FetchAsideOut)
    init.req := false.B
    init.pc := 0.U
    init
  }
  def fetch(addr:UInt):FetchAsideOut = {
    val init = Wire(new FetchAsideOut)
    init.req := true.B
    init.pc := addr
    init
  }
}