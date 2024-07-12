package bus.uart

import chisel3._
import chisel3.util.HasBlackBoxResource

class async_receiver extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val RxD = Input(UInt(1.W))
    val RxD_data_ready = Output(Bool())
    val RxD_clear = Input(Bool())
    val RxD_data = Output(UInt(8.W))
  })
}