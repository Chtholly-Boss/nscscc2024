package ultra
import chisel3._
import ultra.bus.UltraBus
import ultra.bus.sram.SramPorts._
import ultra.caches.FetchPlugin
import ultra.pipeline.fetch.UltraFetchStage
import ultra.pipeline.regfile.Regfile
import ultra.pipeline.decode.UltraDecodeStage
class UltraCpu extends Module {
  val io = IO(new Bundle() {
    val baseSram = new Bundle() {
      val req = Output(new SramRequest)
      val rspns = Input(new SramResponse)
    }
    val extSram = new Bundle() {
      val req = Output(new SramRequest)
      val rspns = Input(new SramResponse)
    }
    val uart = new Bundle() {
      val txd = Output(UInt(1.W))
      val rxd = Input(UInt(1.W))
    }
  })
  val bus = Module(new UltraBus)
  // Interface to the outside
  bus.io.baseRam.in <> io.baseSram.rspns
  bus.io.baseRam.out <> io.baseSram.req
  bus.io.extRam.in <> io.extSram.rspns
  bus.io.extRam.out <> io.extSram.req
  bus.io.uart.rxd := io.uart.rxd
  io.uart.txd := bus.io.uart.txd

  val fetchStage = Module(new UltraFetchStage)
  val fetchPlugin = Module(new FetchPlugin)
  fetchStage.io.aside.in <> fetchPlugin.io.core.out
  fetchStage.io.aside.out <> fetchPlugin.io.core.in
  fetchPlugin.io.bus.in <> bus.io.iChannel.out
  fetchPlugin.io.bus.out <> bus.io.iChannel.in

  val decodeStage = Module(new UltraDecodeStage)
  val regfile = Module(new Regfile)
  decodeStage.io.aside.in.regLeft <> regfile.io.rChannel_1.out
  decodeStage.io.aside.in.regRight <> regfile.io.rChannel_2.out
  decodeStage.io.aside.out <> regfile.io.rChannel_1.in
  decodeStage.io.aside.out <> regfile.io.rChannel_2.in

  decodeStage.io.pipe.fetch.in <> fetchStage.io.pipe.out
  decodeStage.io.pipe.fetch.out <> fetchStage.io.pipe.in

  bus.io.dChannel := DontCare
}
object UltraCpu extends App {
  emitVerilog(new UltraCpu)
}
