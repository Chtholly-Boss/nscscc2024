package elaborate
import chisel3._
import bus.ultra.UltraBusPorts._
import bus.ultra.UltraBus
import caches.icache.Icache
import bus.sram.BaseSram
class IcacheElaborate extends Module{
  val io = IO(new InstSlaveIo)
  val bus = Module(new UltraBus)
  val icache = Module(new Icache)
  val baseRam = Module(new BaseSram)
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
