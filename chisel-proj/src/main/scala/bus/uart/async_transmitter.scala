package bus.uart

import chisel3._

class async_transmitter extends BlackBox {
  val io = IO(new Bundle {
    val clk  = Input(Clock())
    val TxD_start = Input(Bool())
    val TxD_data = Input(UInt(8.W))
    val TxD = Output(UInt(1.W))
    val TxD_busy = Output(Bool())
  })
}