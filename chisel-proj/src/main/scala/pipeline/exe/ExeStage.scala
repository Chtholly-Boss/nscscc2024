package pipeline.exe
import chisel3._
import chisel3.util._
import ExePorts._
import ExeUtils._
import ExeParams._
import ExeParams.{ExeType => tp}
import pipeline.decode.DecodeUtils._

class ExeStage extends Module {
  val io = IO(new ExeIo)
  io.out.ack := false.B
  io.out.exe := initExeOut
  io.aside.out := initExeAsideOut
  io.bCtrl := initExeBranchInfo

  val outReg: ExeOut = RegInit(initExeOut)
  io.out.exe := outReg
  val bCtrlOutReg: ExeBranchInfo = RegInit(initExeBranchInfo)
  io.bCtrl := bCtrlOutReg

  val srcInfo = RegInit(initExeSrcInfo)
  val fetchInfo = RegInit(initDecodeSrcInfo)
  // To deal with Load-to-use
  val preLoad = RegInit(false.B)
  val preLoadAddr = RegInit(0.U(5.W))
  val preLoadData = RegInit(0.U(32.W))

  val alu = Module(new Alu)
  alu.io.in.op := srcInfo.exeOp
  alu.io.in.operand.left := srcInfo.operands.regData_1
  when (srcInfo.operands.hasImm) {
    alu.io.in.operand.right := srcInfo.operands.imm
  } .otherwise {
    alu.io.in.operand.right := srcInfo.operands.regData_2
  }
  object State extends ChiselEnum {
    val
      IDLE,
      ALUEXE,
      BRANCH,
      RD,
      RDWAIT,
      WR,
      WRWAIT = Value
  }
  import State._
  val stat = RegInit(IDLE)
  switch(stat) {
    is (IDLE) {
      outReg := initExeOut
      bCtrlOutReg := initExeBranchInfo

      when (io.in.decode.req) {
        switch (io.in.decode.bits.exeOp.opType) {
          is (tp.branch,tp.jump) {
            stat := BRANCH
          }
          is (tp.load) {
            stat := RD
          }
          is (tp.store) {
            stat := WR
          }
          is (tp.logic,tp.arith,tp.shift) {
            stat := ALUEXE
          }
        }
        when (io.in.decode.bits.wCtrl.en && io.in.decode.bits.wCtrl.addr =/= 0.U) {
          preLoad := true.B
          preLoadAddr := io.in.decode.bits.wCtrl.addr
        } .otherwise {
          preLoad := false.B
        }
        // Cache the inputs
        srcInfo := io.in.decode.bits
        when(preLoad && preLoadAddr === io.in.decode.readInfo.reg_1.addr) {
          srcInfo.operands.regData_1 := preLoadData
        }
        when(preLoad && preLoadAddr === io.in.decode.readInfo.reg_2.addr) {
          srcInfo.operands.regData_2 := preLoadData
        }
        fetchInfo := io.in.decode.fetchInfo
        io.out.ack := true.B
      } .otherwise {
        io.out.ack := false.B
      }
    }
    is (ALUEXE) {
        stat := IDLE
        outReg.bits.en := srcInfo.wCtrl.en
        outReg.bits.addr := srcInfo.wCtrl.addr
        outReg.bits.data := alu.io.out.res
        preLoadData := alu.io.out.res
    }
    is (BRANCH) {
      stat := IDLE
      outReg.bits := srcInfo.wCtrl
      preLoadData := srcInfo.wCtrl.data
      switch (srcInfo.exeOp.opType) {
        is (tp.branch) {
          switch(srcInfo.exeOp.opFunc) {
            is (Branch.bne) {
              when (srcInfo.operands.regData_1.asSInt =/= srcInfo.operands.regData_2.asSInt) {
                bCtrlOutReg.isMispredict := !fetchInfo.predictTaken
                bCtrlOutReg.npc := (fetchInfo.pc.asSInt + srcInfo.operands.imm.asSInt).asUInt
              } .otherwise {
                bCtrlOutReg.isMispredict := fetchInfo.predictTaken
                bCtrlOutReg.npc := fetchInfo.pc + 4.U
              }
            }
            is (Branch.beq) {
              when (srcInfo.operands.regData_1.asSInt === srcInfo.operands.regData_2.asSInt) {
                bCtrlOutReg.isMispredict := !fetchInfo.predictTaken
                bCtrlOutReg.npc := (fetchInfo.pc.asSInt + srcInfo.operands.imm.asSInt).asUInt
              }.otherwise {
                bCtrlOutReg.isMispredict := fetchInfo.predictTaken
                bCtrlOutReg.npc := fetchInfo.pc + 4.U
              }
            }
            is (Branch.bge) {
              when (srcInfo.operands.regData_1.asSInt >= srcInfo.operands.regData_2.asSInt) {
                bCtrlOutReg.isMispredict := !fetchInfo.predictTaken
                bCtrlOutReg.npc := (fetchInfo.pc.asSInt + srcInfo.operands.imm.asSInt).asUInt
              }.otherwise {
                bCtrlOutReg.isMispredict := fetchInfo.predictTaken
                bCtrlOutReg.npc := fetchInfo.pc + 4.U
              }
            }
          }
        }
        is (tp.jump) {
          switch(srcInfo.exeOp.opFunc) {
            is (JumpBranch.jirl) {
              bCtrlOutReg.isMispredict := true.B
              bCtrlOutReg.npc := (srcInfo.operands.regData_1.asSInt + srcInfo.operands.imm.asSInt).asUInt
            }
          }
        }
      }
    }
    is (RD) {
      when (io.aside.in.rrdy) {
        when (srcInfo.exeOp.opFunc === Load.ld_b) {
          switch(alu.io.out.res(1,0)) {
            is ("b00".U) {
              io.aside.out.byteSelN := "b1110".U
            }
            is ("b01".U) {
              io.aside.out.byteSelN := "b1101".U
            }
            is ("b10".U) {
              io.aside.out.byteSelN := "b1011".U
            }
            is ("b11".U) {
              io.aside.out.byteSelN := "b0111".U
            }
          }
        }
        io.aside.out.rreq := true.B
        io.aside.out.addr := alu.io.out.res
        stat := RDWAIT
      }
    }
    is (RDWAIT) {
      io.aside.out.rreq := false.B
      when (io.aside.in.rvalid) {
        stat := IDLE
        outReg.bits.en := srcInfo.wCtrl.en
        outReg.bits.addr := srcInfo.wCtrl.addr
        outReg.bits.data := io.aside.in.rdata
        preLoadData := io.aside.in.rdata
      }
    }
    is (WR) {
      when (io.aside.in.wrdy) {
        when (srcInfo.exeOp.opFunc === Store.st_b) {
          switch(alu.io.out.res(1,0)) {
            is ("b00".U) {
              io.aside.out.byteSelN := "b1110".U
            }
            is ("b01".U) {
              io.aside.out.byteSelN := "b1101".U
            }
            is ("b10".U) {
              io.aside.out.byteSelN := "b1011".U
            }
            is ("b11".U) {
              io.aside.out.byteSelN := "b0111".U
            }
          }
        }
        io.aside.out.wreq := true.B
        io.aside.out.addr := alu.io.out.res
        io.aside.out.wdata := srcInfo.operands.regData_2
        stat := WRWAIT
      }
    }
    is (WRWAIT) {
      io.aside.out.wreq := false.B
      when (io.aside.in.wdone) {
        stat := IDLE
        outReg.bits.en := srcInfo.wCtrl.en
        outReg.bits.addr := srcInfo.wCtrl.addr
        outReg.bits.data := srcInfo.wCtrl.data
      }
    }
  }
}