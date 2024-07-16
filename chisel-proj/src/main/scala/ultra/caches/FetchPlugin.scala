package ultra.caches
import chisel3._
import ultra.pipeline.fetch.UltraFetchPorts.FetchAsidePorts._
import ultra.pipeline.fetch.UltraFetchUtils.FetchAsideUtils._
import ultra.bus.UltraBusPorts._
import bpbuffer.BpBuffer
import ibuffer.Ibuffer
import icache.Icache

class FetchPlugin extends Module{
  val io = IO(new Bundle() {
    val core = new FetchAsideSlaveIo
    val bus = new InstMasterIo
  })
  val icache = Module(new Icache)
  val ibuffer = Module(new Ibuffer)
  val bpBuffer = Module(new BpBuffer)
  // bus-icache Connection
  io.bus.out := icache.io.bus.out
  icache.io.bus.in := io.bus.in
  // icache receive req from core
  icache.io.core.in.pc := io.core.in.pc
  icache.io.core.in.rreq := io.core.in.rreq
  // icache fetch instructions and send to ibuffer,bpbuffer
  // icache-ibuffer
  ibuffer.io.icache.in_pc := icache.io_pc
  ibuffer.io.icache.in := icache.io.core.out
  // icache-bpbuffer
  bpBuffer.io.icache.in := icache.io.core.out
  // ibuffer and bpbuffer receive pc from core and send the infos
  ibuffer.io.core.in.pc := io.core.in.pc
  bpBuffer.io.core.in.pc := io.core.in.pc

  io.core.out.predictTaken := bpBuffer.io.core.out.predictTaken
  io.core.out.pcOffset := bpBuffer.io.core.out.offset
  io.core.out.isInBuf := ibuffer.io.core.out.hit
  io.core.out.rvalid := ibuffer.io.core.out.bits.rvalid
  io.core.out.inst := ibuffer.io.core.out.bits.inst
}
