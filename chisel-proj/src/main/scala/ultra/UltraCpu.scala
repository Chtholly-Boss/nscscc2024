package ultra
import chisel3._
import ultra.bus.UltraBus
import ultra.bus.sram.SramPorts._
import ultra.caches.FetchPlugin
import ultra.caches.ExePlugin
import ultra.pipeline.fetch.UltraFetchStage
import ultra.pipeline.regfile.Regfile
import ultra.pipeline.decode.UltraDecodeStage
import ultra.pipeline.exe.UltraExeStage
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
  decodeStage.io.aside.out.reg_1 <> regfile.io.rChannel_1.in
  decodeStage.io.aside.out.reg_2 <> regfile.io.rChannel_2.in

  decodeStage.io.pipe.fetch.in <> fetchStage.io.pipe.out
  decodeStage.io.pipe.fetch.out <> fetchStage.io.pipe.in

  val exeStage = Module(new UltraExeStage)
  val exePlugin = Module(new ExePlugin)
  exeStage.io.aside.in <> exePlugin.io.core.out
  exeStage.io.aside.out <> exePlugin.io.core.in
  exePlugin.io.bus.out <> bus.io.dChannel.in
  exePlugin.io.bus.in <> bus.io.dChannel.out

  exeStage.io.pipe.decode.in <> decodeStage.io.pipe.exe.out
  exeStage.io.pipe.decode.out <> decodeStage.io.pipe.exe.in
  exeStage.io.pipe.br <> decodeStage.io.pipe.br
  exeStage.io.pipe.br <> fetchStage.io.pipe.br

  exeStage.io.pipe.wback.out.bits <> regfile.io.wChannel
}
object UltraCpu extends App {
  emitVerilog(new UltraCpu)
}
