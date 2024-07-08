package pipeline.fetch
import chisel3._
import FetchPorts._
object FetchUtils {
  def init:FetchOut = {
    val res = Wire(new FetchOut)
    res.req := false.B
    res.bits.pc := 0.U
    res.bits.inst := 0.U
    res
  }
  def initAside:FetchAsideOut = {
    val res = Wire(new FetchAsideOut)
    res.req := false.B
    res.pc := 0.U
    res
  }
  def fetch(addr:UInt):FetchAsideOut = {
    val res = Wire(new FetchAsideOut)
    res.req := true.B
    res.pc := addr
    res
  }
}