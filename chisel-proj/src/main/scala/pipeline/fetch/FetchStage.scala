package pipeline.fetch
import chisel3._
import FetchPorts._
class FetchStage extends Module {
  val io = IO(new FetchIo)

}