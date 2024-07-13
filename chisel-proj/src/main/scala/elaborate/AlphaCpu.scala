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

class AlphaCpu extends Module {
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
  val fetch = Module(new FetchStageBeta)
  val decode = Module(new DecodeStageAlpha)
  val exe = Module(new ExeStage)
  val bus = Module(new NewBetaBus)
  // Peripheral Ports
  bus.io.baseRam.rspns := io.baseSram.rspns
  bus.io.extRam.rspns := io.extSram.rspns
  io.baseSram.req := bus.io.baseRam.req
  io.extSram.req := bus.io.extRam.req
  bus.io.uart.rxd := io.uart.rxd
  io.uart.txd := bus.io.uart.txd
  // Bus-Pipeline Connection
  fetch.io.aside.in <> bus.io.instChannel.rspns
  fetch.io.aside.out <> bus.io.instChannel.req
  exe.io.aside.in <> bus.io.dataChannel.rspns
  exe.io.aside.out <> bus.io.dataChannel.req
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
object AlphaCpu extends App {
  emitVerilog(new AlphaCpu)
}
