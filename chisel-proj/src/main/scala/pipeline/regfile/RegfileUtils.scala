package pipeline.regfile
import chisel3._
import RegfilePorts._

object RegfileUtils {
  def initWctrl:WriteCtrl = {
    val res = Wire(new WriteCtrl)
    res.en := false.B
    res.addr := 0.U
    res.data := 0.U
    res
  }
  def initRctrl:ReadCtrl = {
    val res = Wire(new ReadCtrl)
    res.en := false.B
    res.addr := 0.U
    res
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
