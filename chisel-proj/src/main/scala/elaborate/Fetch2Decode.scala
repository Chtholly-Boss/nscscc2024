package elaborate
import chisel3._
import pipeline.fetch.FetchStage
import pipeline.decode.DecodeStage
import pipeline.regfile.Regfile
import pipeline.decode.DecodePorts.DecodeOut
import bus.sram.BaseSram
import bus.NaiveBus

class Fetch2Decode extends Module {
  val io = IO(new Bundle() {
    val ack = Input(Bool())
    val out = Output(new DecodeOut)
  })
  val regfile = Module(new Regfile)
  val fetch = Module(new FetchStage)
  val decode = Module(new DecodeStage)
  val bus = Module(new NaiveBus)
  val iRom = Module(new BaseSram)

  fetch.io.aside.in <> bus.io.out
  fetch.io.aside.out <> bus.io.in

  bus.io.sram.in <> iRom.io.out
  bus.io.sram.out <> iRom.io.in

  fetch.io.in.ack <> decode.io.out.ack
  fetch.io.out <> decode.io.in.fetch

  decode.io.aside.out.reg_1 <> regfile.io.rChannel_1.in
  decode.io.aside.out.reg_2 <> regfile.io.rChannel_2.in
  decode.io.aside.in.regLeft <> regfile.io.rChannel_1.out
  decode.io.aside.in.regRight <> regfile.io.rChannel_2.out

  regfile.io.wChannel := DontCare

  decode.io.in.ack := io.ack
  io.out := decode.io.out.decode

}
