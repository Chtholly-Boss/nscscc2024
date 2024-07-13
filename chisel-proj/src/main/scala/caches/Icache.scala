package caches
import chisel3._
import chisel3.util._
import caches.IcachePorts._
import caches.IcacheParams._
import pipeline.fetch.FetchUtils._
import caches.blkmem.BlockMem
import caches.blkmem.BlockMemPorts._
class Icache(params: ParamsBundle) extends Module{
  val io = IO(new Bundle {
    val core = new MasterSide
    val bus = new SlaveSide
  })
  io.core.rspns := initFetchAsideIn()
  io.bus.req := initFetchAsideOut
  // Block Memory Ports: Should be replaced by ip core
  val mem = Module(new BlockMem(params.depth,params.dataWidth))
  val memIn = Wire(new BlockMemIn(params.addrWidth,params.dataWidth))
  memIn.addr := 0.U
  memIn.wdata := 0.U
  memIn.wen := false.B
  mem.io.in := memIn

  object State extends ChiselEnum {
    val IDLE = Value
  }
  val stat = RegInit(State.IDLE)
  switch(stat) {
    is (State.IDLE) {

    }
  }
}
