package ultra.pipeline.regfile

import chisel3._
import RegfilePorts._

object RegfileUtils {
  def initWctrl:WriteCtrl = {
    val init = Wire(new WriteCtrl)
    init.en := false.B
    init.addr := 0.U
    init.data := 0.U
    init
  }
  def initRctrl:ReadCtrl = {
    val init = Wire(new ReadCtrl)
    init.en := false.B
    init.addr := 0.U
    init
  }

  def rDataFAddr(addr:UInt):ReadCtrl = {
    val rCtrl = Wire(new ReadCtrl)
    rCtrl.en := true.B
    rCtrl.addr := addr
    rCtrl
  }
  def wData2Addr(wdata:UInt, addr:UInt):WriteCtrl = {
    val wCtrl = Wire(new WriteCtrl)
    wCtrl.en := true.B
    wCtrl.addr := addr
    wCtrl.data := wdata
    wCtrl
  }
}
