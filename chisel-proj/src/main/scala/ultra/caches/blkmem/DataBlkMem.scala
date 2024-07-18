package ultra.caches.blkmem
import chisel3._
import ultra.caches.icache.IcacheParams._
class DataBlkMem extends BlackBox {
  val io = IO(new Bundle() {
    val addra = Input(UInt(indexWidth.W))
    val clka = Input(Clock())
    val dina = Input(UInt(bandwidth.W))
    val douta = Output(UInt(bandwidth.W))
    val wea = Input(Bool())
  })
}
