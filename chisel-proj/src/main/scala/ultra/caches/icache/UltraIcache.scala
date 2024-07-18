package ultra.caches.icache

import chisel3._
import chisel3.util._
import IcacheParams._
import IcachePorts._
import IcacheUtils.{IcacheState => IS}
import ultra.bus.UltraBusUtils._
import ultra.caches.blkmem._

/**
 * This Cache Using Black Box Block Memory
 * Just for synthesize
 */
class UltraIcache extends Module {
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
  val cacheAddr = WireDefault(0.U(indexWidth.W))

  // Tag Block Sram
  val tagRam = Module(new TagBlkMem)
  tagRam.io.clka := clock
  tagRam.io.addra := cacheAddr
  val tagRamWdata = WireDefault(0.U((tagWidth+1).W))
  tagRam.io.wea := cacheWen
  tagRam.io.dina := tagRamWdata
  // Data Block Ram
  val dataRam = Module(new DataBlkMem)
  dataRam.io.clka := clock
  dataRam.io.addra := cacheAddr
  val dataWdata = WireDefault(0.U(bandwidth.W))
  dataRam.io.wea := cacheWen
  dataRam.io.dina := dataWdata

  val iReqBuf = RegInit(initInstReq)
  val stat = RegInit(IS.IDLE)
  when(cacheWen){
    cacheAddr := iReqBuf.pc(indexWidth+offsetWidth-1,offsetWidth)
  }.otherwise{
    cacheAddr := io.core.in.pc(indexWidth+offsetWidth-1,offsetWidth)
  }
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
        iReqBuf := io.core.in
      }
    }
    is(IS.TAG_CHECK){
      when(
        tagRam.io.douta(validBit) === 1.U &&
          tagRam.io.douta(tagWidth-1,0) === iReqBuf.pc(21,indexWidth + offsetWidth)
      ) {
        // Hit
        stat := IS.IDLE
        reqDone(dataRam.io.douta)
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
