package pipeline.fetch
import chisel3._

object FetchParams {
  val pcWidth = 32.W
  val instWidth = 32.W
  val pcRst = "h8000_0000".U(pcWidth)
}