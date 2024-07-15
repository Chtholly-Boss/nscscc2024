package elaborate
import chisel3._
import bus.ultra.UltraBusPorts._
import bus.ultra.UltraBus
import caches.icache.Icache
import bus.sram.BaseSram
import caches.bpbuffer.BpBuffer
import caches.bpbuffer.BpBufferPorts._
import caches.ibuffer.Ibuffer
class IcacheElaborate extends Module{
  val io = IO(new InstSlaveIo)
  val io_pc = IO(Input(UInt(32.W)))
  val io_bpOut = IO(Output(new BpOut))
  val io_ibuffer = IO(Output(new Bundle {
    val hit = Bool()
    val inst = UInt(32.W)
    val rvalid = Bool()
  }))

  val bus = Module(new UltraBus)
  val icache = Module(new Icache)
  val baseRam = Module(new BaseSram)
  val bpBuffer = Module(new BpBuffer)
  val iBuffer = Module(new Ibuffer)
  // Bp-Icache Connection
  bpBuffer.io.icache.in <> icache.io.core.out
  bpBuffer.io.core.in.pc := io_pc
  io_bpOut := bpBuffer.io.core.out
  // Ibuffer-Icache Connection
  iBuffer.io.icache.in <> icache.io.core.out
  iBuffer.io.icache.in_pc <> icache.io_pc
  iBuffer.io.core.in.pc := io_pc
  io_ibuffer.hit := iBuffer.io.core.out.hit
  io_ibuffer.inst := iBuffer.io.core.out.bits.inst
  io_ibuffer.rvalid := iBuffer.io.core.out.bits.rvalid

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
