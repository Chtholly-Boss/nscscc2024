package caches.icache
import chisel3._
import chisel3.util._
import IcacheParams._
import IcachePorts._
import IcacheUtils.{IcacheState => IS}
import bus.ultra.UltraBusUtils._
import caches.blkmem.BlockMem
class Icache extends Module {
  val io = IO(new IcacheIo)
  io.bus.out := initInstReq
  io.core.out := initInstRspns

  val cacheWen = WireDefault(false.B)
  // Tag Block Sram
  val tagRam = Module(new BlockMem(depth,1 + tagWidth))
  tagRam.io.in.addr := io.core.in.pc(indexWidth+offsetWidth-1,offsetWidth)
  val tagRamWdata = WireDefault(0.U(tagWidth.W))
  tagRam.io.in.wen := cacheWen
  tagRam.io.in.wdata := tagRamWdata
  // Data Block Ram
  val dataRam = Module(new BlockMem(depth,bandwidth))
  dataRam.io.in.addr := io.core.in.pc(indexWidth+offsetWidth-1,offsetWidth)
  val dataWdata = WireDefault(0.U(bandwidth.W))
  dataRam.io.in.wen := cacheWen
  dataRam.io.in.wdata := dataWdata

  val iReqBuf = RegInit(initInstReq)
  when(io.core.in.rreq){
    iReqBuf := io.core.in
  }
  val tagBuf = RegInit(0.U(tagWidth.W))
  val dataBuf = RegInit(0.U(bandwidth.W))

  val stat = RegInit(IS.IDLE)
  switch(stat){
    is(IS.IDLE){

    }
    is(IS.TAG_CHECK){

    }
    is(IS.WAIT){
      when(io.bus.in.rvalid){

      }
    }
  }
}
