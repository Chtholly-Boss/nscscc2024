package caches
import chisel3._
import pipeline.fetch.FetchPorts.{FetchAsideIn,FetchAsideOut}
object IcachePorts {
  class MasterSide extends Bundle {
    val req = Input(new FetchAsideOut)
    val rspns = Output(new FetchAsideIn)
  }
  class SlaveSide extends Bundle {
    val req = Output(new FetchAsideOut)
    val rspns = Input(new FetchAsideIn)
  }
}
