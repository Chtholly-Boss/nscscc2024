package elaborate
import chisel3._
import bus.uart._
class Uart extends Module{
  val io = IO(new Bundle() {
    val in = Input(new Bundle() {
      val data = UInt(8.W)
      val start = Bool()
    })
    val out = Output(new Bundle() {
      val data = UInt(8.W)
      val rdy = Bool()
    })
  })
  val transmitter = Module(new async_transmitter)
  val receiver = Module(new async_receiver)

  io.out.data := receiver.io.RxD_data
  io.out.rdy := receiver.io.RxD_data_ready

  transmitter.io.TxD_data := io.in.data
  transmitter.io.TxD_start := io.in.start

  transmitter.io.TxD <> receiver.io.RxD
  transmitter.io.TxD_busy := DontCare
  transmitter.io.clk := clock
  receiver.io.clk := clock
  receiver.io.RxD_clear := DontCare
}
