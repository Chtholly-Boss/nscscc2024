package pipeline.fetch
import chisel3._
import FetchPorts._
object FetchUtils {
  val init = Wire(new FetchOut)
  init.req := false.B
  init.bits.pc := 0.U
  init.bits.inst := 0.U
}