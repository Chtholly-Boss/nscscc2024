package ultra.pipeline.exe
import chisel3._
import chisel3.util._
import UltraExePorts._
import UltraExeUtils._
import UltraExeParams.{ExeType => tp}
import UltraExeParams._
import ultra.pipeline.exe.UltraExePorts.ExeAsidePorts.ExeAsideOut
import ultra.pipeline.exe.UltraExePorts.ExePipePorts.ExeBranchInfo
import ultra.pipeline.regfile.RegfileUtils._
import ultra.pipeline.regfile.RegfilePorts._
class BetaExeStage extends Module {
  val io = IO(new AlphaExeIo)

  def initAllOut() = {
    io.pipe.decode.out.ack := false.B
    io.pipe.wback.out := initExeOut
    io.pipe.br := initExeBranchInfo
    io.aside.out := initExeAsideOut
  }
  initAllOut()
  // alias signals
  val decodeIn = io.pipe.decode.in
  val exeOut = io.pipe.wback.out.bits
  // Define States
  object ExeState extends ChiselEnum {
    val
    IDLE,
    LOADING
    = Value
  }
  import ExeState._
  val exStat = RegInit(IDLE)
  val preWrBuf = RegInit(initWctrl)

  // Operand Determination
  def bypassPreWr(addr:UInt,data:UInt):UInt= {
    val res = WireDefault(data)
    when(preWrBuf.en && (preWrBuf.addr === addr)){
      res := preWrBuf.data
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
  val alu = Module(new UltraAlu)
  alu.io.in.op := decodeIn.bits.exeOp
  alu.io.in.operand.left := regLeft
  alu.io.in.operand.right := aluRight

  val branchTarget = WireDefault(
    (decodeIn.fetchInfo.pc.asSInt + decodeIn.bits.operands.imm.asSInt).asUInt
  )
  val defaultTarget = WireDefault(
    (decodeIn.fetchInfo.pc.asSInt + 4.S).asUInt
  )
  def procBranch(opLeft:UInt,opRight:UInt):ExeBranchInfo = {
    val res = WireDefault(initExeBranchInfo)
    switch(decodeIn.bits.exeOp.opFunc){
      is(Branch.bne){
        when(opLeft =/= opRight) {
          res.isMispredict := !decodeIn.fetchInfo.predictTaken
          res.npc := branchTarget
        }.otherwise{
          res.isMispredict := decodeIn.fetchInfo.predictTaken
          res.npc := defaultTarget
        }
      }
      is(Branch.bge){
        when(opLeft.asSInt >= opRight.asSInt){
          res.isMispredict := !decodeIn.fetchInfo.predictTaken
          res.npc := branchTarget
        }.otherwise{
          res.isMispredict := decodeIn.fetchInfo.predictTaken
          res.npc := defaultTarget
        }
      }
      is(Branch.beq){
        when(opLeft === opRight){
          res.isMispredict := !decodeIn.fetchInfo.predictTaken
          res.npc := branchTarget
        }.otherwise{
          res.isMispredict := decodeIn.fetchInfo.predictTaken
          res.npc := defaultTarget
        }
      }
    }
    res
  }
  val loadInfoBuf = RegInit(initWctrl)
  val asideOutInfo = WireDefault(initExeAsideOut)
  asideOutInfo.rreq := decodeIn.bits.exeOp.opType === tp.load
  asideOutInfo.wreq := decodeIn.bits.exeOp.opType === tp.store
  asideOutInfo.addr := (regLeft.asSInt + decodeIn.bits.operands.imm.asSInt).asUInt
  asideOutInfo.wdata := regRight
  asideOutInfo.byteSelN := WireDefault(0.U(4.W))
  when(decodeIn.bits.exeOp.opFunc === 2.U){
    asideOutInfo.byteSelN := ~(1.U << asideOutInfo.addr(1,0))
  }
  // State Machine Logic
  switch(exStat){
    is(IDLE){
      initAllOut()
      when(decodeIn.req){
        when(decodeIn.bits.exeOp.opType === tp.load){
          when(io.aside.in.rrdy){
            exStat := LOADING
            io.pipe.decode.out.ack := true.B
            io.aside.out := asideOutInfo
            loadInfoBuf := decodeIn.bits.wCtrl
          }
        }.elsewhen(decodeIn.bits.exeOp.opType === tp.store){
          when(io.aside.in.wrdy){
            exStat := IDLE
            io.pipe.decode.out.ack := true.B
            io.aside.out := asideOutInfo
          }
        }.otherwise{
          exStat := IDLE
          io.pipe.decode.out.ack := true.B
          when(decodeIn.bits.wCtrl.addr === 0.U){
            exeOut.en := false.B
            preWrBuf.en := false.B
          }.otherwise{
            exeOut := decodeIn.bits.wCtrl
            preWrBuf := decodeIn.bits.wCtrl
          }
          // Process Arithmetic Operations
          switch(decodeIn.bits.exeOp.opType){
            is(tp.arith){
              exeOut.data := alu.io.out.res
              preWrBuf.data := alu.io.out.res
            }
            is(tp.logic){
              exeOut.data := alu.io.out.res
              preWrBuf.data := alu.io.out.res
            }
            is(tp.shift){
              exeOut.data := alu.io.out.res
              preWrBuf.data := alu.io.out.res
            }
          }
          // Process Branch Jump Operations
          when(decodeIn.bits.exeOp.opType === tp.branch){
            io.pipe.br := procBranch(regLeft,regRight)
          }
          when(decodeIn.bits.exeOp.opType === tp.jump){
            io.pipe.br.isMispredict := true.B
            io.pipe.br.npc := (regLeft.asSInt + decodeIn.bits.operands.imm.asSInt).asUInt
          }
        }
      }
    }
    is(LOADING){
      when(io.aside.in.rvalid){
        exStat := IDLE
        when(loadInfoBuf.addr === 0.U){
          exeOut.en := false.B
          preWrBuf.en := false.B
        }.otherwise{
          exeOut.en := true.B
          exeOut.addr := loadInfoBuf.addr
          exeOut.data := io.aside.in.rdata
          preWrBuf.en := true.B
          preWrBuf.addr := loadInfoBuf.addr
          preWrBuf.data := io.aside.in.rdata
        }
      }
    }
  }
}
