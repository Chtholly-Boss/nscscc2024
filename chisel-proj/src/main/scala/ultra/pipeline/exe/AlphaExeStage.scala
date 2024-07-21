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
class AlphaExeStage extends Module {
  val io = IO(new AlphaExeIo)
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
  // Handy Functions
  def bypassPreWr(addr:UInt,data:UInt):UInt= {
    val res = WireDefault(data)
    when(preWrBuf.en && preWrBuf.addr === addr){
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
  val aluRight = WireDefault(decodeIn.bits.operands.regData_2)
  when(decodeIn.bits.operands.hasImm){
    aluRight := decodeIn.bits.operands.imm
  }

  def initAllOut() = {
    io.pipe.decode.out.ack := false.B
    io.pipe.wback.out := initExeOut
    io.pipe.br := initExeBranchInfo
    io.aside.out := initExeAsideOut
  }
  initAllOut()
  def storePreWrRes(wen:Bool,addr:UInt,data:UInt): Unit = {
    when(addr === 0.U){
      preWrBuf := initWctrl
    }.otherwise{
      preWrBuf.en := wen
      preWrBuf.addr := addr
      preWrBuf.data := data
    }
  }
  def storePreWrRes(wctrl:WriteCtrl) = {
    when(wctrl.addr === 0.U){
      preWrBuf.en := false.B
    }.otherwise{
      preWrBuf := wctrl
    }
  }
  def store2regfile(wen:Bool,addr:UInt,data:UInt) = {
    when(addr === 0.U){
      exeOut.en := false.B
    }.otherwise{
      exeOut.en := wen
      exeOut.addr := addr
      exeOut.data := data
    }
  }
  def store2regfile(wctrl:WriteCtrl) = {
    when(wctrl.addr === 0.U){
      exeOut.en := false.B
    }.otherwise{
      exeOut := wctrl
    }
  }
  def procArith(opLeft:UInt,opRight:UInt):UInt = {
    val res = WireDefault(0.U(32.W))
    switch(decodeIn.bits.exeOp.opFunc){
      is(Arithmetic.add){
        res := (opLeft.asSInt + opRight.asSInt).asUInt
      }
      is(Arithmetic.sltu){
        res := 0.U(31.W) ## (opLeft < opRight).asUInt
      }
    }
    res
  }
  def procLogic(opLeft:UInt,opRight:UInt):UInt = {
    val res = WireDefault(0.U(32.W))
    switch(decodeIn.bits.exeOp.opFunc){
      is(Logic.or){
        res := opLeft | opRight
      }
      is(Logic.and){
        res := opLeft & opRight
      }
      is(Logic.xor){
        res := opLeft ^ opRight
      }
    }
    res
  }

  def procShift(opLeft: UInt, opRight: UInt): UInt = {
    val res = WireDefault(0.U(32.W))
    switch(decodeIn.bits.exeOp.opFunc) {
      is(Shift.srl) {
        res := opLeft >> opRight(4, 0)
      }
    }
    res
  }

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
  def byteMask(func:UInt,addr:UInt):UInt = {
    val byteSelN = WireDefault("b0000".U(4.W))
    when(func === 2.U){
      switch(addr){
        is("b00".U){
          byteSelN := "b1110".U
        }
        is("b01".U){
          byteSelN := "b1101".U
        }
        is("b10".U){
          byteSelN := "b1011".U
        }
        is("b11".U){
          byteSelN := "b0111".U
        }
      }
    }
    byteSelN
  }
  def byteSelInWords(byteSelN:UInt,data:UInt):UInt = {
    val res = WireDefault(data)
    switch(byteSelN){
      is("b1110".U){
        res := data(7,0)
      }
      is("b1101".U){
        res := data(15,8)
      }
      is("b1011".U){
        res := data(23,16)
      }
      is("b0111".U){
        res := data(31,24)
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
  asideOutInfo.byteSelN := byteMask(decodeIn.bits.exeOp.opFunc,asideOutInfo.addr(1,0))
  val asideOutBuf = RegInit(initExeAsideOut)
  // State Machine Logic
  switch(exStat){
    is(IDLE){
      initAllOut()
      when(decodeIn.req){
        switch(decodeIn.bits.exeOp.opType){
          is(tp.nop){
            exStat := IDLE
            io.pipe.decode.out.ack := true.B
          }
          is(tp.arith){
            exStat := IDLE
            io.pipe.decode.out.ack := true.B
            store2regfile(true.B,decodeIn.bits.wCtrl.addr,procArith(regLeft,aluRight))
            storePreWrRes(true.B,decodeIn.bits.wCtrl.addr,procArith(regLeft,aluRight))
          }
          is(tp.logic){
            exStat := IDLE
            io.pipe.decode.out.ack := true.B
            store2regfile(true.B,decodeIn.bits.wCtrl.addr,procLogic(regLeft,aluRight))
            storePreWrRes(true.B,decodeIn.bits.wCtrl.addr,procLogic(regLeft,aluRight))
          }
          is(tp.shift){
            exStat := IDLE
            io.pipe.decode.out.ack := true.B
            store2regfile(true.B,decodeIn.bits.wCtrl.addr,procShift(regLeft,aluRight))
            storePreWrRes(true.B,decodeIn.bits.wCtrl.addr,procShift(regLeft,aluRight))
          }
          is(tp.branch){
            exStat := IDLE
            io.pipe.decode.out.ack := true.B
            store2regfile(decodeIn.bits.wCtrl)
            storePreWrRes(decodeIn.bits.wCtrl)
            io.pipe.br := procBranch(regLeft,regRight)
          }
          is(tp.load){
            when(io.aside.in.rrdy){
              exStat := LOADING
              io.pipe.decode.out.ack := true.B
              io.aside.out := asideOutInfo
              asideOutBuf := asideOutInfo
              loadInfoBuf := decodeIn.bits.wCtrl
            }
          }
          is(tp.store){
            when(io.aside.in.wrdy){
              exStat := IDLE
              io.pipe.decode.out.ack := true.B
              io.aside.out := asideOutInfo
            }
          }
        }
      }
    }
    is(LOADING){
      when(io.aside.in.rvalid){
        exStat := IDLE
        // TODO: Sel bytes in words
        store2regfile(true.B,loadInfoBuf.addr,byteSelInWords(
          asideOutBuf.byteSelN,io.aside.in.rdata
        ))
        storePreWrRes(true.B,loadInfoBuf.addr,byteSelInWords(
          asideOutBuf.byteSelN,io.aside.in.rdata
        ))
      }
    }
  }
}
