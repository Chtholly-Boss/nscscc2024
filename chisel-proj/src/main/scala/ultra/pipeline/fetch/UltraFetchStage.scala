package ultra.pipeline.fetch
import chisel3._
import chisel3.util._
import UltraFetchPorts.FetchIo
import UltraFetchUtils.FetchAsideUtils._
import UltraFetchUtils.FetchPipeUtils._
import UltraFetchParams._
class UltraFetchStage extends Module {
  val io = IO(new FetchIo)
  // Ports Signal Initialization
  val pipeOutReg = RegInit(initFetchOut())
  io.pipe.out := pipeOutReg
  io.aside.out := initFetchAsideOut()
  val npc = RegInit(pcRst)
  io.aside.out.pc := npc
  // Internal Utils
  object FetchState extends ChiselEnum {
    val
      RST,  // rst to init pc
      WAIT, // Wait for buffer refill
      READY, // wait for ack
      MISS // mispredict branch
    = Value
  }
  import FetchState._
  val fstat = RegInit(RST)
  def getInstInBuf():Unit = {
    // Initialize to avoid onchip bugs
    pipeOutReg := initFetchOut()
    io.aside.out.rreq := false.B

    fstat := READY
    pipeOutReg.req := true.B
    pipeOutReg.bits.pc := npc
    pipeOutReg.bits.inst := io.aside.in.inst
    pipeOutReg.bits.predictTaken := io.aside.in.predictTaken
    npc := (io.aside.in.pcOffset.asSInt + npc.asSInt).asUInt
  }
  def refillInstInBuf():Unit = {
    // Initialize to avoid onchip bugs
    pipeOutReg := initFetchOut()
    io.aside.out.rreq := false.B

    fstat := WAIT
    io.aside.out.rreq := true.B
  }

  // Working Logic
  when(io.pipe.br.isMispredict){
    fstat := MISS
    npc := io.pipe.br.npc
    pipeOutReg := initFetchOut()
  }.otherwise{
    switch(fstat){
      is (RST) {
        refillInstInBuf()
      }
      is (WAIT) {
        when(io.aside.in.rvalid){
          getInstInBuf()
        }
      }
      is (READY) {
        when(io.pipe.in.ack){
          when(io.aside.in.isInBuf){
            getInstInBuf()
          }.otherwise{
            refillInstInBuf()
          }
        }
      }
      is (MISS){
        when(io.aside.in.isInBuf){
          getInstInBuf()
        }.otherwise{
          refillInstInBuf()
        }
      }
    }
  }


}
