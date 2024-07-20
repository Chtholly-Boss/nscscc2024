package ultra.pipeline

import chisel3._
import chisel3.util._
import ultra.bus.UltraBusPorts._
import ultra.bus.UltraBusUtils._
import ultra.caches.blkmem._
import ultra.caches.icache.IcacheParams._
import ultra.pipeline.decode.UltraDecodeParams.{_2RI16 => Inst}
import ultra.pipeline.fetch.UltraFetchPorts._
import ultra.pipeline.fetch.UltraFetchUtils.FetchAsideUtils._
class UltraFetchPlugin extends Module {
  val io = IO(new Bundle() {
    val core = new FetchAsidePorts.UltraFetchAsideSlaveIo
    val bus = new InstMasterIo
  })
  io.core.out := initUltraFetchAsideIn()
  io.bus.out := initInstReq
  object State extends ChiselEnum {
    val
    IDLE,
    SCAN,
    SEND,
    WAIT,
    PADDING
    = Value
  }
  import State._
  val fpstat = RegInit(IDLE)
  // Memory
  val tagRam = Module(new TagBlkMem)
  val dataRam = Module(new DataBlkMem)
  val cache_we = WireDefault(false.B)
  val cache_addr = WireDefault(0.U(indexWidth.W))
  val tagWdata = WireDefault(0.U((1+tagWidth).W))
  val lineWdata = WireDefault(0.U(bandwidth.W))
  tagRam.io.clka := clock
  tagRam.io.addra := cache_addr
  tagRam.io.wea := cache_we
  tagRam.io.dina := tagWdata
  dataRam.io.clka := clock
  dataRam.io.addra := cache_addr
  dataRam.io.wea  := cache_we
  dataRam.io.dina := lineWdata
  // request buffer
  val iReqBuf = RegInit(initUltraFetchAsideOut())
  val nReqBuf = RegInit(initUltraFetchAsideOut()) // newest req
  when(io.core.in.rreq){
    nReqBuf := io.core.in
  }
  val isHit = WireDefault(
    tagRam.io.douta(validBit) === 1.U &&
      tagRam.io.douta(tagWidth-1,0) === pc2tag(nReqBuf.pc)
  )
  val instSel = WireDefault(0.U(32.W))
  instSel := dataRam.io.douta >> (nReqBuf.pc(offsetWidth-1,0) << 3)
  // Handy Functions
  def pc2cacheAddr(pc:UInt):UInt = {
    pc(offsetWidth+indexWidth,offsetWidth)
  }
  def pc2tag(pc:UInt):UInt = {
    pc(21,offsetWidth+indexWidth)
  }
  def predictBranch(pc:UInt,inst:UInt):(UInt,Bool) = {
    val npc = WireDefault(pc + 4.U)
    val taken = WireDefault(false.B)
    switch(inst(31,26)){
      is(Inst.beq,Inst.bge,Inst.bne){
        when(inst(25) === 1.U){
          taken := true.B
          npc := (pc.asSInt + (inst(25,10) ## 0.U(2.W)).asSInt).asUInt
        }
      }
      is(Inst.bl,Inst.b_){
        taken := true.B
        npc := (pc.asSInt + (inst(9,0) ## inst(25,10) ## 0.U(2.W)).asSInt).asUInt
      }
    }
    (npc,taken)
  }
  def ackReq() = {
    cache_addr := pc2cacheAddr(io.core.in.pc)
    cache_we := false.B
  }
  def hitAction(pcCur:UInt,inst:UInt) = {
    io.core.out.isHit := true.B
    io.core.out.npc := predictBranch(pcCur,inst)._1
    io.core.out.predictTaken := predictBranch(pcCur,inst)._2
    io.core.out.inst := inst
  }
  def refillAction(pc:UInt) = {
    io.bus.out.rreq := true.B
    io.bus.out.pc := pc
  }
  // State Transition and Output
  switch(fpstat){
    is(IDLE){
      when(io.core.in.rreq){
        fpstat := SCAN
        ackReq()
      }
    }
    is(SCAN){
      when(isHit){
        hitAction(nReqBuf.pc,instSel)

        /**
         * Pipeline implemented by State Machine:
         * When in Done state,a new req come,ack it!
         */
        when(io.core.in.rreq){
          ackReq()
          fpstat := SCAN
        }.otherwise{
          fpstat := IDLE
        }
      }.otherwise{
        when(io.bus.in.rrdy){
          refillAction(nReqBuf.pc)
          iReqBuf := nReqBuf
          fpstat := WAIT
        }.otherwise{
          fpstat := SEND
        }
      }
    }
    is(SEND){
      when(io.bus.in.rrdy){
        refillAction(iReqBuf.pc)
        fpstat := WAIT
      }.otherwise{
        fpstat := SEND
      }
    }
    is(WAIT){
      when(io.bus.in.rvalid){
        cache_we := true.B
        cache_addr := pc2cacheAddr(iReqBuf.pc)
        tagWdata := 1.U(1.W) ## pc2tag(iReqBuf.pc)
        lineWdata := io.bus.in.rdata
        fpstat := PADDING
      }
    }
    is(PADDING){
      cache_we := false.B
      cache_addr := pc2cacheAddr(nReqBuf.pc)
      fpstat := SCAN
    }
  }
}
