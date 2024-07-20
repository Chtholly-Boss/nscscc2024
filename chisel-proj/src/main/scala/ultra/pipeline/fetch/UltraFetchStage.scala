package ultra.pipeline.fetch
import chisel3._
import chisel3.util._
import UltraFetchPorts.UltraFetchIo
import UltraFetchUtils.FetchPipeUtils._
import UltraFetchUtils.FetchAsideUtils._
import UltraFetchParams._
class UltraFetchStage extends Module {
  val io = IO(new UltraFetchIo)
  val pipeOutReg = RegInit(initFetchOut())
  io.pipe.out := pipeOutReg
  io.aside.out := initFetchAsideOut()
  object State  extends ChiselEnum{
    val
      RST,
      WAIT,
      READY
    = Value
  }
  import State._
  val fstat = RegInit(RST)
  val pcCur = RegInit(pcRst)
  // Handy Functions
  def rstAllOut() = {
    io.aside.out := initUltraFetchAsideOut()
    pipeOutReg := initFetchOut()
  }
  def sendReq(pc:UInt) = {
    io.aside.out.rreq := true.B
    io.aside.out.pc := pc
  }
  def rstAction() = {
    rstAllOut()
    sendReq(pcRst)
    fstat := WAIT
  }
  def misPredictAction() = {
    rstAllOut()
    sendReq(io.pipe.br.npc)
    pcCur := io.pipe.br.npc
  }
  def hitAction() = {
    rstAllOut()
    fstat := READY
    pipeOutReg.req := true.B
    pipeOutReg.bits.pc := pcCur
    pipeOutReg.bits.inst := io.aside.in.inst
    pipeOutReg.bits.predictTaken := io.aside.in.predictTaken
    // Update the pcCur tp npc and send npc to cache
    pcCur := io.aside.in.npc
  }
  def missAction() = {
    rstAllOut()
    sendReq(pcCur)
    fstat := WAIT
  }
  when(io.pipe.br.isMispredict){
    misPredictAction()
    fstat := WAIT
  }.otherwise{
    switch(fstat){
      is(RST){
        rstAction()
      }
      is(WAIT){
        when(io.aside.in.isHit){
          hitAction()
        }
      }
      is(READY){
        when(io.pipe.in.ack){
          when(io.aside.in.isHit){
            hitAction()
            sendReq(io.aside.in.npc)
          }.otherwise{
            missAction()
          }
        }
      }
    }
  }
}
