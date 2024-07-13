package pipeline.fetch
import chisel3._
import chisel3.util._
import FetchPorts._
import FetchUtils._
import FetchParams._
class FetchStageAlpha extends Module {
  val io = IO(new FetchIo)
  io.aside.out := initFetchAsideOut
  io.out := initFetchOut

  val outReg = RegInit(initFetchOut)
  io.out := outReg

  object State extends ChiselEnum {
    val
    RST,
    SEND,
    WAIT,
    PCGEN
    = Value
  }
  import State._

  val stat = RegInit(RST)

  when(io.bCtrl.isMispredict) {
    stat := SEND
    outReg.req := false.B
    outReg.bits.pc := io.bCtrl.npc
  } .otherwise {
    switch (stat) {
      is (RST) {
        outReg.bits.pc := pcRst
        stat := SEND
      }
      is (SEND) {
        when (io.aside.in.rrdy) {
          stat := WAIT
          io.aside.out := fetch(outReg.bits.pc)
        }
      }
      is (WAIT) {
        when (io.aside.in.rvalid) {
          stat := PCGEN
          outReg.bits.inst := io.aside.in.inst
          outReg.req := true.B
        }
      }
      is (PCGEN) {
        when (io.in.ack) {
          outReg.req := false.B
          stat := SEND
          // Generate new pc: May need a pcGen
          outReg.bits.pc := outReg.bits.pc + 4.U
        }
      }
    }
  }
}
