package ultra.pipeline.decode
import chisel3._
import UltraDecodePorts.DecodePipePorts._
import UltraDecodePorts.DecodeAsidePorts._
import UltraDecodePorts.DecoderPorts._
import ultra.pipeline.regfile.RegfileUtils._
import ultra.pipeline.exe.UltraExeUtils._
object UltraDecodeUtils {
  def initDecoderRes:DecoderRes = {
    val init = Wire(new DecoderRes)
    init.reg_1 := initRctrl
    init.reg_2 := initRctrl
    init.wCtrl := initWctrl
    init.hasImm := 0.U
    init.imm := 0.U
    init.exeOp := initOp
    init
  }
  def initDecodeAsideIn:DecodeAsideIn = {
    val init = Wire(new DecodeAsideIn)
    init.regLeft := 0.U
    init.regRight := 0.U
    init
  }
  def initDecodeAsideOut:DecodeAsideOut = {
    val init = Wire(new DecodeAsideOut)
    init.reg_1 := initRctrl
    init.reg_2 := initRctrl
    init
  }
  def initDecodeSrcInfo:DecodeSrcInfo = {
    val init = Wire(new DecodeSrcInfo)
    init.pc := 0.U
    init.inst := 0.U
    init.predictTaken := false.B
    init
  }

  def initDecodeOut: DecodePipeOut = {
    val init = Wire(new DecodePipeOut)
    init.bits := initExeSrcInfo
    init.req := false.B
    init.fetchInfo := initDecodeSrcInfo
    init.readInfo.reg_1 := initRctrl
    init.readInfo.reg_2 := initRctrl
    init
  }
}
