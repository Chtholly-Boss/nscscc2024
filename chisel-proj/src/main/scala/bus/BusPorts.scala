package bus

import chisel3._
import bus.sram.SramPorts._
import pipeline.fetch.FetchPorts.{FetchAsideIn,FetchAsideOut}
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
    val req = Input(new Bundle() {
      val rReq = Bool()
      val wReq = Bool()
      val addr = UInt(32.W)
      val wData = UInt(32.W)
    })
    val rspns = Output(new Bundle() {
      val data = UInt(32.W)
      val rdy = Bool()
      val done = Bool()
    })
  }
}
