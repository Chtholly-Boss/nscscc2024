package ultra.pipeline.decode
import chisel3._
import UltraDecodePorts.DecoderPorts._
import ultra.helper.MultiMux1
import ultra.pipeline.decode.decoders.{BaseDecoder, Decoder_2RI12, Decoder_2RI16, Decoder_3R, Decoder_Special}

class Du extends BaseDecoder {
  val decoders =
    Seq(
      Module(new Decoder_2RI12),
      Module(new Decoder_2RI16),
      Module(new Decoder_3R),
      Module(new Decoder_Special)
    )
  decoders.foreach(_.io.in := io.in)
  val dMux = Module(new MultiMux1(decoders.length,new DecoderRes,0.U.asTypeOf(new DecoderRes)))
  dMux.io.inputs.zip(decoders).foreach {
    case (dst,src) =>
      dst.valid := src.io.out.isMatched
      dst.bits := src.io.out.bits
  }
  io.out.isMatched := dMux.io.output.valid
  io.out.bits := dMux.io.output.bits
}
