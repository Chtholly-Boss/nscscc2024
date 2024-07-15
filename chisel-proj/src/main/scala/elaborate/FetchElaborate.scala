package elaborate
import chisel3._
import pipeline.fetch.FetchPorts.FetchOut
import bus.sram._
import bus.ultra.UltraBus
import pipeline.ultra.fetch.UltraFetchStage
import caches.icache.Icache
import caches.ibuffer.Ibuffer
import caches.bpbuffer.BpBuffer

class FetchElaborate extends Module {
  val io = IO(new Bundle() {
    val in = Input(new Bundle() {
      val ack = Bool()
    })
    val out = Output(new FetchOut)
  })
  val fetchStage = Module(new UltraFetchStage)
  val icache = Module(new Icache)
  val ibuffer = Module(new Ibuffer)
  val bpBuffer = Module(new BpBuffer)
  val bus = Module(new UltraBus)
  val baseRam = Module(new BaseSram)
  val extRam = Module(new ExtSram)

  fetchStage.io.aside.in.pcOffset := bpBuffer.io.core.out.offset
  fetchStage.io.aside.in.predictTaken := bpBuffer.io.core.out.predictTaken
  bpBuffer.io.core.in.pc := fetchStage.io.aside.out.pc
  bpBuffer.io.icache.in <> icache.io.core.out

  fetchStage.io.aside.in.inst := ibuffer.io.core.out.bits.inst
  fetchStage.io.aside.in.rvalid := ibuffer.io.core.out.bits.rvalid
  fetchStage.io.aside.in.isInBuf := ibuffer.io.core.out.hit
  ibuffer.io.core.in.pc := fetchStage.io.aside.out.pc
  ibuffer.io.icache.in <> icache.io.core.out
  ibuffer.io.icache.in_pc <> icache.io_pc

  icache.io.core.in.pc := fetchStage.io.aside.out.pc
  icache.io.core.in.rreq := fetchStage.io.aside.out.rreq
  icache.io.bus.in <> bus.io.iChannel.out
  icache.io.bus.out <> bus.io.iChannel.in

  bus.io.baseRam.in <> baseRam.io.out
  bus.io.baseRam.out <> baseRam.io.in
  bus.io.extRam.in <> extRam.io.out
  bus.io.extRam.out <> extRam.io.in

  fetchStage.io.bCtrl := DontCare

  bus.io.dChannel := DontCare
  bus.io.uart := DontCare

  io.out := fetchStage.io.out
  fetchStage.io.in := io.in
}
