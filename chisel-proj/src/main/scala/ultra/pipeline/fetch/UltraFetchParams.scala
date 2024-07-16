package ultra.pipeline.fetch
import chisel3._
object UltraFetchParams {
  val pcWidth = 32.W
  val instWidth = 32.W
  val pcRst = "h8000_0000".U(pcWidth)
}
