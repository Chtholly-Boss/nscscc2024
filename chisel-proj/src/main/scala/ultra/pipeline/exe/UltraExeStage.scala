package ultra.pipeline.exe

/**
 * This Stage is specialized to do the accelerate on hardware
 * You should use this stage when you want to solve the problem in Chisel
 */

import chisel3._
import chisel3.util._
import UltraExePorts._
import UltraExeUtils._
import UltraExeParams.{ExeType => tp}
import UltraExeParams._
import ultra.pipeline.regfile.RegfileUtils.initWctrl
import ultra.bus.UltraBusParams._
class UltraExeStage extends Module {
  val io = IO(new AlphaExeIo)
  def initAllOut() = {
    io.pipe.decode.out.ack := false.B
    io.pipe.wback.out := initExeOut
    io.pipe.br := initExeBranchInfo
    io.aside.out := initExeAsideOut
  }
  initAllOut()
  // Alias the IO signals
  val decodeIn = io.pipe.decode.in
  val exeOut = io.pipe.wback.out
  val ack = io.pipe.decode.out.ack

  val preWrBuf = RegInit(initExeOut)
  preWrBuf := exeOut
  // Operand Determination
  def bypassPreWr(addr:UInt,data:UInt):UInt= {
    val res = WireDefault(data)
    when(preWrBuf.bits.en && (preWrBuf.bits.addr === addr)){
      res := preWrBuf.bits.data
    }
    res
  }
  val regLeft = WireDefault(
    bypassPreWr(decodeIn.readInfo.reg_1.addr,decodeIn.bits.operands.regData_1)
  )
  val regRight = WireDefault(
    bypassPreWr(decodeIn.readInfo.reg_2.addr,decodeIn.bits.operands.regData_2)
  )
  val aluRight = WireDefault(regRight)
  when(decodeIn.bits.operands.hasImm){
    aluRight := decodeIn.bits.operands.imm
  }
  val branchTarget = WireDefault(
    (decodeIn.fetchInfo.pc.asSInt + decodeIn.bits.operands.imm.asSInt).asUInt
  )
  val defaultTarget = WireDefault(
    (decodeIn.fetchInfo.pc.asSInt + 4.S).asUInt
  )
  //***************************************************
  //***************************************************
  //***************************************************
  object RS extends ChiselEnum {
    val
    IDLE,
    BLOCK,
    RUNNING
    = Value
  }
  val rStat = RegInit(RS.IDLE)
  val rWrBuf = RegInit(initWctrl)
  val rAwHazard = WireDefault(
    decodeIn.readInfo.reg_1.addr === rWrBuf.addr ||
      decodeIn.readInfo.reg_2.addr === rWrBuf.addr
  )
  val wAwHazard = WireDefault(
    decodeIn.bits.wCtrl.addr === rWrBuf.addr
  )
  val rHasHazard = WireDefault(rAwHazard || wAwHazard)
  val rReqBuf = RegInit(initExeAsideOut)
  switch(rStat){
    is(RS.IDLE){
      // Determined by the req process
    }
    is(RS.BLOCK){
      when(io.aside.in.rrdy){
        io.aside.out := rReqBuf
        rStat := RS.RUNNING
      }
    }
    is(RS.RUNNING){
      when(io.aside.in.rvalid){
        rStat := RS.IDLE
        exeOut.bits.en := true.B
        when(rWrBuf.addr === 0.U){
          exeOut.bits.en := false.B
        }
        exeOut.bits.addr := rWrBuf.addr
        exeOut.bits.data := io.aside.in.rdata
      }
    }
  }
  //****************************************************
  //***************************************************
  //***************************************************
  object WS extends ChiselEnum {
    val
    IDLE,
    BLOCK
    = Value
  }
  val wStat = RegInit(WS.IDLE)
  val wReqBuf = RegInit(initExeAsideOut)
  switch(wStat){
    is(WS.IDLE){
      // Determined by the req process
    }
    is(WS.BLOCK){
      when(io.aside.in.wrdy && rStat === RS.IDLE){
        io.aside.out := wReqBuf
        wStat := WS.IDLE
      }
    }
  }
  //****************************************************
  //***************************************************
  //***************************************************
  object MDS extends ChiselEnum {
    // Multiplication or Division
    val
      IDLE,
      Div_WORK,
      Div_DONE
    = Value
  }

  val mStat = RegInit(MDS.IDLE)
  val mWrBuf = RegInit(initWctrl)
  val mrAwHazard = WireDefault(
    decodeIn.readInfo.reg_1.addr === mWrBuf.addr ||
      decodeIn.readInfo.reg_2.addr === mWrBuf.addr
  )
  val mwAwHazard = WireDefault(
    decodeIn.bits.wCtrl.addr === mWrBuf.addr
  )
  val mHasHazard = WireDefault(mrAwHazard || mwAwHazard)
  // Add your hardware accelerator here
  val divU = Module(new DivU)
  divU.io.in.valid := false.B
  divU.io.in.bits.left := regLeft
  divU.io.in.bits.right := regRight
  val quotientBuf = RegInit(0.U(32.W))
  val remainderBuf = RegInit(0.U(32.W))
  switch(mStat){
    is(MDS.IDLE){
    }
    is(MDS.Div_WORK){
      // Buf your result here
      when(divU.io.out.valid){
        mStat := MDS.Div_DONE
        quotientBuf := divU.io.out.bits.quotient
      }
    }
    is(MDS.Div_DONE){
      when(io.aside.in.rvalid){
      }.otherwise{
        mStat := MDS.IDLE
        exeOut.bits.en := true.B
        when(mWrBuf.addr === 0.U){
          exeOut.bits.en := false.B
        }
        exeOut.bits.addr := mWrBuf.addr
        // output your result here
        exeOut.bits.data := quotientBuf
      }
    }
  }

  //****************************************************
  //***************************************************
  //***************************************************
  // Default aside out Info
  val asideOutDefault = WireDefault(initExeAsideOut)
  asideOutDefault.rreq := decodeIn.bits.exeOp.opType === tp.load
  asideOutDefault.wreq := decodeIn.bits.exeOp.opType === tp.store
  asideOutDefault.addr := (regLeft.asSInt + decodeIn.bits.operands.imm.asSInt).asUInt
  asideOutDefault.wdata := regRight
  asideOutDefault.byteSelN := 0.U(4.W)
  when(decodeIn.bits.exeOp.opFunc === 2.U){
    asideOutDefault.byteSelN := ~(1.U << asideOutDefault.addr(1,0))
  }
  // Request Process
  when(decodeIn.req){
    when(io.aside.in.rvalid){
      // Load is Writing
    }.elsewhen(
      mStat === MDS.Div_DONE
    ){
      // Mul or Div is Writing
    }.otherwise{
      when(rStat =/= RS.IDLE && rHasHazard){
        // Do nothing
      }.elsewhen(mStat =/= MDS.IDLE && mHasHazard){
        // Do nothing
      }.otherwise{
        switch(decodeIn.bits.exeOp.opType){
          is(tp.nop){
            ack := true.B
          }
          is(tp.arith){
            when(decodeIn.bits.exeOp.opFunc === Arithmetic.div){
              when(mStat === MDS.IDLE){
                ack := true.B
                divU.io.in.valid := true.B
                mStat := MDS.Div_WORK
                mWrBuf := decodeIn.bits.wCtrl
              }
            }.otherwise{
              ack := true.B
              exeOut.bits.en := true.B
              when(decodeIn.bits.wCtrl.addr === 0.U){
                exeOut.bits.en := false.B
              }
              exeOut.bits.addr := decodeIn.bits.wCtrl.addr
              switch(decodeIn.bits.exeOp.opFunc){
                is(Arithmetic.add){
                  exeOut.bits.data := (regLeft.asSInt + aluRight.asSInt).asUInt
                }
                is(Arithmetic.sub){
                  exeOut.bits.data := (regLeft.asSInt - aluRight.asSInt).asUInt
                }
                is(Arithmetic.sltu){
                  exeOut.bits.data := (regLeft < aluRight).asUInt
                }
              }
            }

          }
          is(tp.logic){
            ack := true.B
            exeOut.bits.en := true.B
            when(decodeIn.bits.wCtrl.addr === 0.U){
              exeOut.bits.en := false.B
            }
            exeOut.bits.addr := decodeIn.bits.wCtrl.addr
            switch(decodeIn.bits.exeOp.opFunc){
              is(Logic.and){
                exeOut.bits.data := regLeft & aluRight
              }
              is(Logic.or){
                exeOut.bits.data := regLeft | aluRight
              }
              is(Logic.xor){
                exeOut.bits.data := regLeft ^ aluRight
              }
            }
          }
          is(tp.shift){
            ack := true.B
            exeOut.bits.en := true.B
            when(decodeIn.bits.wCtrl.addr === 0.U){
              exeOut.bits.en := false.B
            }
            exeOut.bits.addr := decodeIn.bits.wCtrl.addr
            switch(decodeIn.bits.exeOp.opFunc){
              is(Shift.sll){
                exeOut.bits.data := regLeft << aluRight(4,0)
              }
              is(Shift.srl){
                exeOut.bits.data := regLeft >> aluRight(4,0)
              }
            }
          }
          is(tp.branch){
            ack := true.B
            exeOut.bits.en := true.B
            when(decodeIn.bits.wCtrl.addr === 0.U){
              exeOut.bits.en := false.B
            }
            exeOut.bits.addr := decodeIn.bits.wCtrl.addr
            exeOut.bits.data := decodeIn.bits.wCtrl.data
            switch(decodeIn.bits.exeOp.opFunc){
              is(Branch.beq){
                when(regLeft === regRight){
                  io.pipe.br.isMispredict := !decodeIn.fetchInfo.predictTaken
                  io.pipe.br.npc := branchTarget
                }.otherwise{
                  io.pipe.br.isMispredict := decodeIn.fetchInfo.predictTaken
                  io.pipe.br.npc := defaultTarget
                }
              }
              is(Branch.bne){
                when(regLeft =/= regRight){
                  io.pipe.br.isMispredict := !decodeIn.fetchInfo.predictTaken
                  io.pipe.br.npc := branchTarget
                }.otherwise{
                  io.pipe.br.isMispredict := decodeIn.fetchInfo.predictTaken
                  io.pipe.br.npc := defaultTarget
                }
              }
              is(Branch.bge){
                when(regLeft.asSInt >= regRight.asSInt){
                  io.pipe.br.isMispredict := !decodeIn.fetchInfo.predictTaken
                  io.pipe.br.npc := branchTarget
                }.otherwise{
                  io.pipe.br.isMispredict := decodeIn.fetchInfo.predictTaken
                  io.pipe.br.npc := defaultTarget
                }
              }
            }
          }
          is(tp.jump){
            ack := true.B
            exeOut.bits.en := true.B
            when(decodeIn.bits.wCtrl.addr === 0.U){
              exeOut.bits.en := false.B
            }
            exeOut.bits.addr := decodeIn.bits.wCtrl.addr
            exeOut.bits.data := decodeIn.bits.wCtrl.data
            io.pipe.br.isMispredict := true.B
            io.pipe.br.npc := (regLeft.asSInt + decodeIn.bits.operands.imm.asSInt).asUInt
          }
          is(tp.load){
            when(rStat === RS.IDLE){
              ack := true.B
              rReqBuf := asideOutDefault
              rWrBuf := decodeIn.bits.wCtrl
              when(io.aside.in.rrdy){
                rStat := RS.RUNNING
                io.aside.out := asideOutDefault
              }.otherwise{
                rStat := RS.BLOCK
              }
            }.otherwise{
              // Wait until rStat turn to IDLE
            }
          }
          is(tp.store){
            when(wStat === WS.IDLE){
              ack := true.B
              wReqBuf := asideOutDefault
              when(io.aside.in.wrdy && rStat === RS.IDLE){
                io.aside.out := asideOutDefault
              }.otherwise{
                wStat := WS.BLOCK
              }
            }.otherwise{
              // wait until wStat turn to IDLE
            }
          }
        }
      }
    }
  }
}
