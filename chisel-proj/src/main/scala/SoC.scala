import chisel3._
import ultra.UltraCpu
import ultra.bus.sram.{BaseSram, ExtSram}
import ultra.bus.sram.SramPorts._

class SoC extends Module {
  val io = IO(new Bundle() {
    val baseSram = Output(new Bundle() {
      val req = new SramRequest
      val rspns = new SramResponse
    })
    val extSram = Output(new Bundle() {
      val req = new SramRequest
      val rspns = new SramResponse
    })
  })
  val cpu = Module(new UltraCpu)
  val baseRam = Module(new BaseSram("./func/bintests/lab2.txt"))
  val extRam = Module(new ExtSram("./func/bintests/matrix.txt"))

  cpu.io.baseSram.req <> baseRam.io.in
  cpu.io.baseSram.rspns <> baseRam.io.out
  cpu.io.extSram.req <> extRam.io.in
  cpu.io.extSram.rspns <> extRam.io.out

  cpu.io.uart.rxd := DontCare
  cpu.io.uart.txd := DontCare

  // Debug Signals
  io.baseSram.req := cpu.io.baseSram.req
  io.baseSram.rspns := baseRam.io.out
  io.extSram.req := cpu.io.extSram.req
  io.extSram.rspns := extRam.io.out
}
