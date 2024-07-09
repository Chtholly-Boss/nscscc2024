package pipeline.decode.decoders
import chisel3._
import pipeline.decode.DecodePorts._
import pipeline.decode.DecodeUtils._
class BaseDecoder extends Module {
  val io = IO(new DecoderIo)
  io.out.bits := initDecoderRes
  io.out.isMatched := false.B
}
