package ultra.pipeline.decode.decoders

import chisel3._
import ultra.pipeline.decode.UltraDecodePorts.DecoderPorts._
import ultra.pipeline.decode.UltraDecodeUtils._
class BaseDecoder extends Module {
  val io = IO(new DecoderIo)
  io.out.bits := initDecoderRes
  io.out.isMatched := false.B
}
