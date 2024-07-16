package ultra.bus.uart

import chisel3._

class async_receiver(f:Int = 60 * math.pow(10,6).toInt)
  extends BlackBox(Map("ClkFrequency" -> f)) {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val RxD = Input(UInt(1.W))
    val RxD_data_ready = Output(Bool())
    val RxD_clear = Input(Bool())
    val RxD_data = Output(UInt(8.W))
  })
}