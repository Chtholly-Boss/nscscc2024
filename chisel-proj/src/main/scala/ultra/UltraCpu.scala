package ultra
import chisel3._
import ultra.bus.UltraBus
import ultra.bus.sram.SramPorts._
import ultra.caches._
import ultra.pipeline.fetch._
import ultra.pipeline.regfile.Regfile
import ultra.pipeline.decode._
import ultra.pipeline.exe._
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
  val fetchPlugin = Module(new UltraFetchPlugin)
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

  val exeStage = Module(new ExeStage)
  exeStage.io.aside.in <> bus.io.dChannel.out
  exeStage.io.aside.out <> bus.io.dChannel.in

  exeStage.io.pipe.decode.in <> decodeStage.io.pipe.exe.out
  exeStage.io.pipe.decode.out <> decodeStage.io.pipe.exe.in
  exeStage.io.pipe.br <> decodeStage.io.pipe.br
  exeStage.io.pipe.br <> fetchStage.io.pipe.br

  exeStage.io.pipe.wback.out.bits <> regfile.io.wChannel
}
object UltraCpu extends App {
  emitVerilog(new UltraCpu)
}
