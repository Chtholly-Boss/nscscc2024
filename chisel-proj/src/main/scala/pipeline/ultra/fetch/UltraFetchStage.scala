package pipeline.ultra.fetch
import chisel3._
import chisel3.util._
import pipeline.fetch.FetchPorts._
import pipeline.fetch.FetchUtils.{fetch, initFetchOut}
import pipeline.exe.ExePorts._
import pipeline.fetch.UltraFetchPorts._
import pipeline.fetch.UltraFetchUtils._
import pipeline.fetch.UltraFetchParams._
import pipeline.fetch.UltraFetchUtils.{FetchState => FS}
class UltraFetchStage extends Module {
  val io = IO(new Bundle {
    val in = Input(new Bundle {
      val ack = Bool()
    })
    val aside = new UltraFetchAsideMasterIo
    val out = Output(new FetchOut)
    val bCtrl = Input(new ExeBranchInfo)
  })
  io.aside.out := initFetchAsideOut()
  val npc = RegInit(pcRst)
  io.aside.out.pc := npc // check if the next pc in ibuffer
  val fetchOutReg = RegInit(initFetchOut)
  io.out := fetchOutReg

  val fstat = RegInit(FS.RST)
  def instGet() = {
    fetchOutReg := initFetchOut
    fstat := FS.READY
    fetchOutReg.req := true.B
    fetchOutReg.bits.pc := npc
    fetchOutReg.bits.inst := io.aside.in.inst
    fetchOutReg.bits.predictTaken := io.aside.in.predictTaken
    npc := (io.aside.in.pcOffset.asSInt + npc.asSInt).asUInt
  }
  def instMiss():Unit = {
    fetchOutReg := initFetchOut
    fstat := FS.WAIT
    io.aside.out.rreq := true.B
  }
  switch(fstat){
    is(FS.RST){
      instMiss()
    }
    is(FS.WAIT){
      when(io.aside.in.rvalid){
        instGet()
      }
    }
    is(FS.READY){
      when(io.in.ack){
        when(io.aside.in.isInBuf){
          instGet()
        } .otherwise {
          instMiss()
        }
      }
    }
  }
}
