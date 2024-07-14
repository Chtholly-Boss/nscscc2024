package pipeline.fetch
import chisel3._
import chisel3.util._
import FetchPorts._
import FetchUtils._
import FetchParams._
import pipeline.decode.DecodeParam.{_2RI16 => Inst}
class FetchStageBeta extends Module {
  val io = IO(new FetchIo(4 * 32))
  io.aside.out := initFetchAsideOut
  io.out := initFetchOut

  val outReg = RegInit(initFetchOut)
  io.out := outReg
  val npc = RegInit(0.U(32.W))
  val instBuffer = RegInit(0.U(128.W))
  val curInst = Wire(UInt(128.W))
  curInst := 0.U
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
          curInst := io.aside.in.inst >> (npc(3,0) << 3)
          stat := PCGEN
          instBuffer := io.aside.in.inst
          outReg.bits.inst := curInst(31,0)
          outReg.req := true.B
          // BTFNT Branch Prediction
          outReg.bits.predictTaken := false.B
          npc := outReg.bits.pc + 4.U
          switch (curInst(31,0)) {
            is (Inst.beq,Inst.bge,Inst.bne) {
              when (curInst(25) === 1.U) {
                outReg.bits.predictTaken := true.B
                npc := (outReg.bits.pc.asSInt +
                  (curInst(25,10) ## 0.U(2.W)).asSInt
                  ).asUInt
              }
            }
            is (Inst.bl,Inst.b_) {
              outReg.bits.predictTaken := true.B
              npc := (outReg.bits.pc.asSInt +
                (curInst(9,0) ## curInst(25,10) ## 0.U(2.W)).asSInt
                ).asUInt
            }
          }

        }
      }
      is (PCGEN) {
        when (io.in.ack) {
          when (npc(21,4) === outReg.bits.pc(21,4)) {
            curInst := instBuffer >> (npc(3,0) << 3)
            stat := PCGEN
            outReg.bits.pc := npc
            outReg.bits.inst := curInst(31,0)
            outReg.req := true.B
            npc := outReg.bits.pc + 4.U
            switch (curInst(31,0)) {
              is (Inst.beq,Inst.bge,Inst.bne) {
                when (curInst(25) === 1.U) {
                  outReg.bits.predictTaken := true.B
                  npc := (outReg.bits.pc.asSInt +
                    (curInst(25,10) ## 0.U(2.W)).asSInt
                    ).asUInt
                }
              }
              is (Inst.bl,Inst.b_) {
                outReg.bits.predictTaken := true.B
                npc := (outReg.bits.pc.asSInt +
                  (curInst(9,0) ## curInst(25,10) ## 0.U(2.W)).asSInt
                  ).asUInt
              }
            }
          } .otherwise {
            outReg.req := false.B
            stat := SEND
            // Generate new pc: May need a pcGen
            //outReg.bits.pc := outReg.bits.pc + 4.U
            outReg.bits.pc := npc
          }
        }
      }
    }
  }
}