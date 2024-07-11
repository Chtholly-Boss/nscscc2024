FIRRTL version 1.2.0
circuit Uart :
  extmodule async_transmitter : @[src/main/scala/bus/uart/async_transmitter.scala 5:7]
    input clk : Clock
    input TxD_start : UInt<1>
    input TxD_data : UInt<8>
    output TxD : UInt<1>
    output TxD_busy : UInt<1>
    defname = async_transmitter

  extmodule async_receiver : @[src/main/scala/bus/uart/async_receiver.scala 6:7]
    input clk : Clock
    input RxD : UInt<1>
    output RxD_data_ready : UInt<1>
    input RxD_clear : UInt<1>
    output RxD_data : UInt<8>
    defname = async_receiver

  module Uart : @[src/main/scala/elaborate/Uart.scala 4:7]
    input clock : Clock @[src/main/scala/elaborate/Uart.scala 4:7]
    input reset : UInt<1> @[src/main/scala/elaborate/Uart.scala 4:7]
    input io_in_data : UInt<8> @[src/main/scala/elaborate/Uart.scala 5:14]
    input io_in_start : UInt<1> @[src/main/scala/elaborate/Uart.scala 5:14]
    output io_out_data : UInt<8> @[src/main/scala/elaborate/Uart.scala 5:14]
    output io_out_rdy : UInt<1> @[src/main/scala/elaborate/Uart.scala 5:14]

    inst transmitter of async_transmitter @[src/main/scala/elaborate/Uart.scala 15:27]
    inst receiver of async_receiver @[src/main/scala/elaborate/Uart.scala 16:24]
    io_out_data <= receiver.RxD_data @[src/main/scala/elaborate/Uart.scala 18:15]
    io_out_rdy <= receiver.RxD_data_ready @[src/main/scala/elaborate/Uart.scala 19:14]
    transmitter.clk <= clock @[src/main/scala/elaborate/Uart.scala 26:22]
    transmitter.TxD_start <= io_in_start @[src/main/scala/elaborate/Uart.scala 22:28]
    transmitter.TxD_data <= io_in_data @[src/main/scala/elaborate/Uart.scala 21:27]
    receiver.clk <= clock @[src/main/scala/elaborate/Uart.scala 27:19]
    receiver.RxD <= transmitter.TxD @[src/main/scala/elaborate/Uart.scala 24:22]
    receiver.RxD_clear is invalid
