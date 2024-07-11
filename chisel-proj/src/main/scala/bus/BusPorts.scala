package bus

import chisel3._
import bus.sram.SramPorts._
import pipeline.fetch.FetchPorts.{FetchAsideIn,FetchAsideOut}
import pipeline.exe.ExePorts.{ExeAsideIn,ExeAsideOut}
object BusPorts {
  class SramChannel extends Bundle {
    val rspns = Input(new SramResponse)
    val req = Output(new SramRequest)
  }

  class InstChannel extends Bundle {
    val req = Input(new FetchAsideOut)
    val rspns = Output(new FetchAsideIn)
  }

  class DataChannel extends Bundle {
    val req = Input(new ExeAsideOut)
    val rspns = Output(new ExeAsideIn)
  }
  class UartIo extends Bundle {
    val txd = Output(UInt(1.W))
    val rxd = Input(UInt(1.W))
  }
}
