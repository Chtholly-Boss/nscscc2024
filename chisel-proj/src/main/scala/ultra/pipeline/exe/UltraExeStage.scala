package ultra.pipeline.exe
import chisel3._
import chisel3.util._
import UltraExePorts.ExeIo
import UltraExeUtils._
import UltraExeParams.{ExeType => tp}
import UltraExeParams._
class UltraExeStage extends Module {
  val io = IO(new ExeIo)
  // alias the decode info
  val decode = io.pipe.decode.in

  val pipeOutReg = RegInit(initExeOut)
  io.pipe.wback.out := pipeOutReg
  val preResBuf = RegInit(initExeOut)
  object ExeState extends ChiselEnum {
    val
    IDLE,
    LOAD_BLOCK,
    LOADING,
    STORE_BLOCK,
    STORING
    = Value
  }
  import ExeState._
  val exstat = RegInit(IDLE)
  def default() = {
    exstat := IDLE
    io.pipe.decode.out.ack := false.B
    pipeOutReg := initExeOut
    io.pipe.br := initExeBranchInfo
    io.aside.out := initExeAsideOut
  }
  default()

  def diffRegWithPreWrite(addr:UInt,data:UInt):UInt = {
    val res = WireDefault(0.U(32.W))
    when(
      preResBuf.bits.en &&
        preResBuf.bits.addr === addr
    ){
      res := preResBuf.bits.data
    }.otherwise {
      res := data
    }
    res
  }
  val regLeft = Wire(UInt(32.W))
  val regRight = Wire(UInt(32.W))
  regLeft := diffRegWithPreWrite(
    decode.readInfo.reg_1.addr,decode.bits.operands.regData_1
  )
  regRight := diffRegWithPreWrite(
    decode.readInfo.reg_2.addr,decode.bits.operands.regData_2
  )

  val alu = Module(new Alu)
  alu.io.in.op := io.pipe.decode.in.bits.exeOp
  alu.io.in.operand.left := regLeft
  when(decode.bits.operands.hasImm){
    alu.io.in.operand.right := decode.bits.operands.imm
  }.otherwise{
    alu.io.in.operand.right := regRight
  }


  def store2PreRes(en:Bool,addr:UInt,data:UInt) = {
    when(addr === 0.U){
      preResBuf := initExeOut
    }.otherwise{
      preResBuf.bits.en := en
      preResBuf.bits.addr := addr
      preResBuf.bits.data := data
    }
  }
  // Process Compute
  def processComp() = {
    exstat := IDLE
    pipeOutReg.bits.en := true.B
    pipeOutReg.bits.addr := decode.bits.wCtrl.addr
    pipeOutReg.bits.data := alu.io.out.res
    store2PreRes(true.B,decode.bits.wCtrl.addr,alu.io.out.res)
  }
  // Process Branch
  val branchTarget = WireDefault(
    decode.fetchInfo.pc.asSInt + decode.bits.operands.imm.asSInt
  ).asUInt
  val defaultTarget = WireDefault(
    decode.fetchInfo.pc.asSInt + 4.S
  ).asUInt
  def processBranch() = {
    exstat := IDLE
    pipeOutReg.bits := decode.bits.wCtrl
    store2PreRes(decode.bits.wCtrl.en,decode.bits.wCtrl.addr,decode.bits.wCtrl.data)
    switch(decode.bits.exeOp.opFunc){
      is (Branch.bne) {
        when(regLeft =/= regRight){
          io.pipe.br.npc := branchTarget
          io.pipe.br.isMispredict := !decode.fetchInfo.predictTaken
        }.otherwise{
          io.pipe.br.npc := defaultTarget
          io.pipe.br.isMispredict := decode.fetchInfo.predictTaken
        }
      }
      is (Branch.bge) {
        when(regLeft.asSInt >= regRight.asSInt){
          io.pipe.br.npc := branchTarget
          io.pipe.br.isMispredict := !decode.fetchInfo.predictTaken
        }.otherwise{
          io.pipe.br.npc := defaultTarget
          io.pipe.br.isMispredict := decode.fetchInfo.predictTaken
        }
      }
      is (Branch.beq) {
        when(regLeft === regRight){
          io.pipe.br.npc := branchTarget
          io.pipe.br.isMispredict := !decode.fetchInfo.predictTaken
        }.otherwise{
          io.pipe.br.npc := defaultTarget
          io.pipe.br.isMispredict := decode.fetchInfo.predictTaken
        }
      }
    }
  }
  // Process Jump
  def processJump() = {
    exstat := IDLE
    pipeOutReg.bits := decode.bits.wCtrl
    store2PreRes(decode.bits.wCtrl.en,decode.bits.wCtrl.addr,decode.bits.wCtrl.data)
    io.pipe.br.isMispredict := true.B
    io.pipe.br.npc := (regLeft.asSInt + decode.bits.operands.imm.asSInt).asUInt
  }
  // Process Load/Store
  val asideOutWire = WireDefault(initExeAsideOut)
  asideOutWire.addr := alu.io.out.res
  asideOutWire.rreq := decode.bits.exeOp.opType === tp.load
  asideOutWire.wreq := decode.bits.exeOp.opType === tp.store
  asideOutWire.byteSelN := "b0000".U
  asideOutWire.wdata := regRight
  val asideOutBuf = RegInit(initExeAsideOut)
  def selByte(addr:UInt):UInt = {
    val byteSelN = WireDefault(0.U(4.W))
    switch(addr(1,0)){
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
    byteSelN
  }
  def selByteInWords(byteSelN:UInt,data:UInt):UInt = {
    val res = WireDefault(0.U(32.W))
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
      is("b0000".U){
        res := data
      }
    }
    res
  }
  // Select a path based on opType
  def exePathSelect() = {
    switch(decode.bits.exeOp.opType){
      is(tp.arith,tp.shift,tp.logic){
        processComp()
      }
      is(tp.branch){
        processBranch()
      }
      is(tp.jump){
        processJump()
      }
      is(tp.load){
        asideOutBuf := asideOutWire
        when(decode.bits.exeOp.opFunc === Load.ld_b){
          asideOutBuf.byteSelN := selByte(asideOutWire.addr)
        }
        when(io.aside.in.rrdy){
          io.aside.out := asideOutWire
          when(decode.bits.exeOp.opFunc === Load.ld_b){
            io.aside.out.byteSelN := selByte(asideOutWire.addr)
          }
          exstat := LOADING
        }.otherwise{
          exstat := LOAD_BLOCK
        }
      }
      is(tp.store){
        asideOutBuf := asideOutWire
        when(decode.bits.exeOp.opFunc === Store.st_b){
          asideOutBuf.byteSelN := selByte(asideOutWire.addr)
        }
        when(io.aside.in.wrdy){
          exstat := STORING
          io.aside.out := asideOutWire
          when(decode.bits.exeOp.opFunc === Store.st_b){
            io.aside.out.byteSelN := selByte(asideOutWire.addr)
          }
        }.otherwise{
          exstat := STORE_BLOCK
        }
      }
    }
  }

  val curWctrlBuf = RegInit(initExeOut)
  switch(exstat){
    is(IDLE){
      default()
      when(decode.req){
        io.pipe.decode.out.ack := true.B
        curWctrlBuf.bits := decode.bits.wCtrl
        exePathSelect()
      }
    }
    is(LOAD_BLOCK){
      default()
      when(io.aside.in.rrdy){
        exstat := LOADING
        io.aside.out := asideOutBuf
      }.otherwise{
        exstat := LOAD_BLOCK
      }
    }
    is(LOADING){
      default()
      when(io.aside.in.rvalid){
        exstat := IDLE
        pipeOutReg := curWctrlBuf
        pipeOutReg.bits.data := selByteInWords(asideOutBuf.byteSelN,io.aside.in.rdata)
        store2PreRes(true.B,curWctrlBuf.bits.addr,selByteInWords(asideOutBuf.byteSelN,io.aside.in.rdata))
      }.otherwise{
        exstat := LOADING
      }
    }
    is(STORE_BLOCK){
      default()
      when(io.aside.in.wrdy){
        exstat := STORING
        io.aside.out := asideOutBuf
      }.otherwise{
        exstat := STORE_BLOCK
      }
    }
    is(STORING){
      default()
      when(io.aside.in.wdone){
        when(decode.req){
          io.pipe.decode.out.ack := true.B
          curWctrlBuf.bits := decode.bits.wCtrl
          exePathSelect()
        }
      }.otherwise{
        exstat := STORING
      }
    }
  }
}
