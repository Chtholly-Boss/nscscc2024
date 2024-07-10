package bus
import chisel3._
import bus.BusPorts._
import bus.sram.SramUtils._
class AlphaBus extends Module {
  val io = IO(new Bundle() {
    val instChannel = new InstChannel
    val dataChannel = new DataChannel
    val baseRam = new SramChannel
    val extRam = new SramChannel
  })

}