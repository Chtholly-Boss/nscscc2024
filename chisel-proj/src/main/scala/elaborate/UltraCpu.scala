package elaborate
import chisel3._
import pipeline.fetch._
import pipeline.decode.DecodeStage
import pipeline.decode.DecodeStageAlpha
import pipeline.exe.ExeStage
import pipeline.regfile.Regfile
import bus.sram._
import bus.sram.SramPorts._
import bus._
import pipeline.exe.ExePorts._
import bus.ultra.UltraBus
import pipeline.fetch.UltraFetchStage
import pipeline.exe.UltraExeStage

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
  val regfile = Module(new Regfile)
  val fetch = Module(new UltraFetchStage)
  val decode = Module(new DecodeStageAlpha)
  val exe = Module(new UltraExeStage)
  val bus = Module(new UltraBus)
  // Peripheral Ports
  bus.io.baseRam.in := io.baseSram.rspns
  bus.io.extRam.in := io.extSram.rspns
  io.baseSram.req := bus.io.baseRam.out
  io.extSram.req := bus.io.extRam.out
  bus.io.uart.rxd := io.uart.rxd
  io.uart.txd := bus.io.uart.txd
  // Bus-Pipeline Connection
  fetch.io.aside.in <> bus.io.iChannel.out
  fetch.io.aside.out <> bus.io.iChannel.in
  exe.io.aside.in <> bus.io.dChannel.out
  exe.io.aside.out <> bus.io.dChannel.in
  // Core Connection
  // Regfile Connection
  regfile.io.rChannel_1.in <> decode.io.aside.out.reg_1
  regfile.io.rChannel_2.in <> decode.io.aside.out.reg_2
  regfile.io.rChannel_1.out <> decode.io.aside.in.regLeft
  regfile.io.rChannel_2.out <> decode.io.aside.in.regRight
  regfile.io.wChannel <> exe.io.out.exe.bits
  // Pipeline Connection
  fetch.io.in.ack <> decode.io.out.ack
  fetch.io.out <> decode.io.in.fetch
  decode.io.in.ack <> exe.io.out.ack
  decode.io.out.decode <> exe.io.in.decode
  // Branch MisPrediction
  fetch.io.bCtrl <> exe.io.bCtrl
  decode.io.bCtrl <> exe.io.bCtrl

}
object UltraCpu extends App {
  emitVerilog(new UltraCpu)
}
