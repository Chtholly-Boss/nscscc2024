package elaborate
import chisel3._
import pipeline.fetch.FetchPorts.FetchOut
import pipeline.fetch.FetchStage
import bus.sram.BaseSram
import bus.NaiveBus
class FetchElaborate extends Module {
  val io = IO(new Bundle() {
    val in = Input(new Bundle() {
      val ack = Bool()
    })
    val out = Output(new FetchOut)
  })
  val fetchStage = Module(new FetchStage)
  val bus = Module(new NaiveBus)
  val baseRam = Module(new BaseSram)

  fetchStage.io.aside.in <> bus.io.out
  fetchStage.io.aside.out <> bus.io.in

  io.out := fetchStage.io.out
  fetchStage.io.in := io.in

  bus.io.sram.in <> baseRam.io.out
  bus.io.sram.out <> baseRam.io.in
}
