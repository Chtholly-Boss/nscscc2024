package ultra
import chisel3._
import ultra.bus.{GammaBus, UltraBus}
import ultra.bus.sram.SramPorts._
import ultra.pipeline.Pipeline
class Cpu extends Module {
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
  val bus = Module(new GammaBus)
  // Interface to the outside
  bus.io.baseRam.in <> io.baseSram.rspns
  bus.io.baseRam.out <> io.baseSram.req
  bus.io.extRam.in <> io.extSram.rspns
  bus.io.extRam.out <> io.extSram.req
  bus.io.uart.rxd := io.uart.rxd
  io.uart.txd := bus.io.uart.txd

  val pipeline = Module(new Pipeline)
  pipeline.io.iChannel.in <> bus.io.iChannel.out
  pipeline.io.iChannel.out <> bus.io.iChannel.in
  pipeline.io.dChannel.in <> bus.io.dChannel.out
  pipeline.io.dChannel.out <> bus.io.dChannel.in
}