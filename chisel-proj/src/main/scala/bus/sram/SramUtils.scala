package bus.sram
import chisel3._
import bus.sram.SramParam._
import bus.sram.SramPorts._
object SramUtils {
  def initSramReq:SramRequest = {
    val init = Wire(new SramRequest)
    init.ce := highLevel
    init.oe := highLevel
    init.we := highLevel
    init.byteSelN := "b1111".U
    init.addr := 0.U
    init.wData := 0.U
    init
  }

  def sramReadWord(addr: Bits) : SramRequest = {
    val sramReadWord = Wire(new SramRequest)
    sramReadWord.ce := lowLevel
    sramReadWord.oe := lowLevel
    sramReadWord.we := highLevel
    sramReadWord.byteSelN := "b0000".U
    sramReadWord.addr := addr
    sramReadWord.wData := 0.U
    sramReadWord
  }

  def sramWrite(addr:Bits,data:Bits) : SramRequest = {
    val sramWriteWord = Wire(new SramRequest)
    sramWriteWord.ce := lowLevel
    sramWriteWord.oe := lowLevel
    sramWriteWord.we := highLevel
    sramWriteWord.byteSelN := "b0000".U
    sramWriteWord.addr := addr
    sramWriteWord.wData := 0.U
    sramWriteWord
  }
}