package ultra.caches.blkmem
import chisel3._
import ultra.caches.icache.IcacheParams._
class TagBlkMem extends BlackBox {
  val dataWidth = tagWidth + 1
  val io = IO(new Bundle() {
    val addra = Input(UInt(indexWidth.W))
    val clka = Input(Clock())
    val dina = Input(UInt(dataWidth.W))
    val douta = Output(UInt(dataWidth.W))
    val wea = Input(Bool())
  })
}
