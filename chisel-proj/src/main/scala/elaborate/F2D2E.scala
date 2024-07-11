package elaborate
import chisel3._
import pipeline.fetch.FetchStage
import pipeline.decode.DecodeStage
import pipeline.exe.ExeStage
import pipeline.regfile.Regfile
import pipeline.exe.ExePorts.ExeOut
import bus.sram._
import bus._
import pipeline.exe.ExePorts._

class F2D2E extends Module {
  val io = IO(new Bundle() {
    val in = Input(new Bundle() {
      val ack = Bool()
      val exeAside = new ExeAsideIn
    })
    val out = Output(new Bundle() {
      val exeBctrl = new ExeBranchInfo
      val exeOut = new ExeOut
      val exeAside = new ExeAsideOut
      val txd = Bool()
    })
  })
  val regfile = Module(new Regfile)
  val fetch = Module(new FetchStage)
  val decode = Module(new DecodeStage)
  val exe = Module(new ExeStage)
  val bus = Module(new BetaBus)
  //val bus = Module(new AlphaBus)
  val iRom = Module(new BaseSram)
  val dRam = Module(new SramSim)
  // Bus Connection
  fetch.io.aside.in <> bus.io.instChannel.rspns
  fetch.io.aside.out <> bus.io.instChannel.req
  exe.io.aside.in <> bus.io.dataChannel.rspns
  exe.io.aside.out <> bus.io.dataChannel.req
  bus.io.baseRam.rspns <> iRom.io.out
  bus.io.baseRam.req <> iRom.io.in
  bus.io.extRam.rspns <> dRam.io.out
  bus.io.extRam.req <> dRam.io.in
  // Pipeline Connection
  fetch.io.in.ack <> decode.io.out.ack
  fetch.io.bCtrl <> exe.io.bCtrl
  fetch.io.out <> decode.io.in.fetch

  decode.io.aside.out.reg_1 <> regfile.io.rChannel_1.in
  decode.io.aside.out.reg_2 <> regfile.io.rChannel_2.in
  decode.io.aside.in.regLeft <> regfile.io.rChannel_1.out
  decode.io.aside.in.regRight <> regfile.io.rChannel_2.out

  decode.io.bCtrl <> exe.io.bCtrl
  decode.io.in.ack <> exe.io.out.ack
  decode.io.out.decode <> exe.io.in.decode

  exe.io.out.exe.bits <> regfile.io.wChannel

  io.out.exeOut := exe.io.out.exe
  io.out.exeAside := exe.io.aside.out
  io.out.exeBctrl := exe.io.bCtrl
  io.out.txd := bus.io.uart.txd
  bus.io.uart.rxd := DontCare
}
object F2D2E extends App {
  emitVerilog(new F2D2E)
}