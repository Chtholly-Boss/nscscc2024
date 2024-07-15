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
  // supplement to ibuffer and bpunits
  val io_pc = IO(Output(UInt(32.W)))
  val pcOutReg = RegInit(0.U(32.W))
  io_pc := pcOutReg

  val rspns2coreReg = RegInit(initInstRspns)
  val req2busReg = RegInit(initInstReq)
  io.core.out := rspns2coreReg
  io.bus.out := req2busReg

  val cacheWen = WireDefault(false.B)
  // Tag Block Sram
  val tagRam = Module(new BlockMem(depth,1 + tagWidth))
  tagRam.io.in.addr := io.core.in.pc(indexWidth+offsetWidth-1,offsetWidth)
  val tagRamWdata = WireDefault(0.U((tagWidth+1).W))
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
  val stat = RegInit(IS.IDLE)

  def reqDone(data:Bits) = {
    rspns2coreReg.rvalid := true.B
    rspns2coreReg.rdata := data
    // Send for record and predict
    pcOutReg := iReqBuf.pc
  }
  switch(stat){
    is(IS.IDLE){
      rspns2coreReg.rvalid := false.B
      when(io.core.in.rreq){
        stat := IS.TAG_CHECK
      }
    }
    is(IS.TAG_CHECK){
      when(
        tagRam.io.out.rdata(validBit) === 1.U &&
          tagRam.io.out.rdata(tagWidth-1,0) === iReqBuf.pc(21,indexWidth + offsetWidth)
      ) {
        // Hit
        stat := IS.IDLE
        reqDone(dataRam.io.out.rdata)
      }.otherwise {
        when(io.bus.in.rrdy){
          stat := IS.WAIT
          req2busReg := iReqBuf
        }.otherwise{
          stat := IS.SEND
        }
      }
    }
    is(IS.SEND){
      when(io.bus.in.rrdy){
        stat := IS.WAIT
        req2busReg := iReqBuf
      }
    }
    is(IS.WAIT){
      req2busReg := initInstReq
      when(io.bus.in.rvalid){
        cacheWen := true.B
        tagRamWdata := 1.U(1.W) ## iReqBuf.pc(21,offsetWidth + indexWidth)
        dataWdata := io.bus.in.rdata
        reqDone(io.bus.in.rdata)
        stat := IS.IDLE
      }
    }
  }
}
