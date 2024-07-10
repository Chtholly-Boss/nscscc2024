package pipeline.fetch
import chisel3._
import FetchPorts._
object FetchUtils {
  def initFetchOut:FetchOut = {
    val init = Wire(new FetchOut)
    init.req := false.B
    init.bits.pc := 0.U
    init.bits.inst := 0.U
    init
  }
  def initFetchAsideIn:FetchAsideIn = {
    val init = Wire(new FetchAsideIn)
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