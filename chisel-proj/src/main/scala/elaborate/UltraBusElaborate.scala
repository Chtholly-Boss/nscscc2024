package elaborate
import chisel3._
import ultra.bus.UltraBusPorts._
import ultra.bus.UltraBus
import ultra.bus.sram.BaseSram

class UltraBusElaborate extends Module {
  val io = IO(new Bundle() {
    val inst = new InstSlaveIo
    val data = new DataSlaveIo
  })
  val bus = Module(new UltraBus)
  val baseRam = Module(new BaseSram)
  bus.io.iChannel.in := io.inst.in
  io.inst.out := bus.io.iChannel.out
  bus.io.dChannel.in := io.data.in
  io.data.out := bus.io.dChannel.out

  bus.io.baseRam.in <> baseRam.io.out
  bus.io.baseRam.out <> baseRam.io.in

  bus.io.uart := DontCare
  bus.io.extRam := DontCare
}
