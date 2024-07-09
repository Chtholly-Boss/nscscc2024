package pipeline.wback

import chisel3._
import WBackPorts._
import pipeline.regfile.RegfileUtils.{initWctrl => RegWriteInit}

object WBackUtils {
  val init = Wire(new WBackOut)
  init.ack := false.B
  init.bits := RegWriteInit
}