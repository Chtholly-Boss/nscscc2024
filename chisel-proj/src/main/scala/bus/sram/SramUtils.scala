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
    val sig = Wire(new SramRequest)
    sig.ce := lowLevel
    sig.oe := lowLevel
    sig.we := highLevel
    sig.byteSelN := "b0000".U
    sig.addr := addr
    sig.wData := 0.U
    sig
  }

  def sramRead(addr:Bits,bSelN:Bits):SramRequest = {
    val sig = Wire(new SramRequest)
    sig.ce := lowLevel
    sig.oe := lowLevel
    sig.we := highLevel
    sig.byteSelN := bSelN
    sig.addr := addr
    sig.wData := 0.U
    sig
  }

  def sramWrite(addr:Bits,data:Bits,bSelN:Bits) : SramRequest = {
    val sig = Wire(new SramRequest)
    sig.ce := lowLevel
    sig.oe := highLevel
    sig.we := lowLevel
    sig.byteSelN := bSelN
    sig.addr := addr
    sig.wData := data
    sig
  }
}