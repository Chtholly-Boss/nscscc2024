package elaborate
import chisel3._
import bus.ultra.UltraBusPorts._
import bus.ultra.UltraBus
import caches.icache.Icache
import bus.sram.BaseSram
import caches.bpbuffer.BpBuffer
import caches.bpbuffer.BpBufferPorts._
class IcacheElaborate extends Module{
  val io = IO(new InstSlaveIo)
  val io_pc = IO(Input(UInt(32.W)))
  val io_bpOut = IO(Output(new BpOut))

  val bus = Module(new UltraBus)
  val icache = Module(new Icache)
  val baseRam = Module(new BaseSram)
  val bpBuffer = Module(new BpBuffer)
  bpBuffer.io.icache.in <> icache.io.core.out
  bpBuffer.io.fetch.in.pc := io_pc
  io_bpOut := bpBuffer.io.fetch.out

  bus.io.iChannel.out <> icache.io.bus.in
  bus.io.iChannel.in <> icache.io.bus.out
  bus.io.baseRam.in <> baseRam.io.out
  bus.io.baseRam.out <> baseRam.io.in

  bus.io.extRam := DontCare
  bus.io.uart := DontCare
  bus.io.dChannel := DontCare

  io.out := icache.io.core.out
  icache.io.core.in := io.in
}
