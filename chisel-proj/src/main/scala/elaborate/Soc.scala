package elaborate
import chisel3._
import ultra.bus.sram.{BaseSram, SramSim}
import ultra.bus.sram.SramPorts._
class Soc extends Module {
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
  val baseRam = Module(new BaseSram)
  val extRam = Module(new SramSim)

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
