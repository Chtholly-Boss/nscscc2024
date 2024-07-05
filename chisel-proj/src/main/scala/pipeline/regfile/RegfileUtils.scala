package pipeline.regfile
import chisel3._
import RegfilePorts._

object RegfileUtils {
  val wInit = Wire(new WriteCtrl)
  wInit.en := false.B
  wInit.addr := 0.U
  wInit.data := 0.U
  val rInit = Wire(new ReadCtrl)
  rInit.en := false.B
  rInit.addr := 0.U

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
