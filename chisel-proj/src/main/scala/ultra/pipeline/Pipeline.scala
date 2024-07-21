package ultra.pipeline
import chisel3._
import ultra.bus.UltraBusPorts._
import ultra.caches.FetchPlugin
import fetch._
import decode._
import exe._
import regfile._
class Pipeline extends Module {
  val io = IO(new Bundle() {
    val iChannel = new InstMasterIo
    val dChannel = new DataMasterIo
  })
  val fetchStage = Module(new UltraFetchStage)
  val fetchPlugin = Module(new FetchPlugin)
  fetchStage.io.aside.in <> fetchPlugin.io.core.out
  fetchStage.io.aside.out <> fetchPlugin.io.core.in
  fetchPlugin.io.bus.in := io.iChannel.in
  io.iChannel.out := fetchPlugin.io.bus.out

  val decodeStage = Module(new UltraDecodeStage)
  val regfile = Module(new Regfile)
  decodeStage.io.aside.in.regLeft <> regfile.io.rChannel_1.out
  decodeStage.io.aside.in.regRight <> regfile.io.rChannel_2.out
  decodeStage.io.aside.out.reg_1 <> regfile.io.rChannel_1.in
  decodeStage.io.aside.out.reg_2 <> regfile.io.rChannel_2.in

  decodeStage.io.pipe.fetch.in <> fetchStage.io.pipe.out
  decodeStage.io.pipe.fetch.out <> fetchStage.io.pipe.in

  val exeStage = Module(new BetaExeStage)
  exeStage.io.aside.in := io.dChannel.in
  io.dChannel.out := exeStage.io.aside.out

  exeStage.io.pipe.decode.in <> decodeStage.io.pipe.exe.out
  exeStage.io.pipe.decode.out <> decodeStage.io.pipe.exe.in
  exeStage.io.pipe.br <> fetchStage.io.pipe.br
  exeStage.io.pipe.br <> decodeStage.io.pipe.br
  exeStage.io.pipe.wback.out.bits <> regfile.io.wChannel
}
