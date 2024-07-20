package ultra.pipeline.exe
import chisel3._
import chisel3.util._
import UltraExePorts._
import UltraExeUtils._
import UltraExeParams.{ExeType => tp}
import UltraExeParams._
import ultra.pipeline.exe.UltraExePorts.ExeAsidePorts.ExeAsideOut
class ExeStage extends Module {
  val io = IO(new AlphaExeIo)
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
    STORE_BLOCK
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
  asideOutWire.wdata := regRight
  when(
    decode.bits.exeOp.opFunc === Store.st_b ||
      decode.bits.exeOp.opFunc === Load.ld_b
  ){
    asideOutWire.byteSelN := selByte(asideOutWire.addr)
  }.otherwise{
    asideOutWire.byteSelN := "b0000".U
  }
  val asideOutBuf = RegInit(initExeAsideOut)
  def processStore(req:ExeAsideOut) = {
    exstat := IDLE
    io.aside.out := req
  }
  def processLoad(req:ExeAsideOut) = {
    exstat := LOADING
    io.aside.out := req
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
        loadWctrlBuf.bits := decode.bits.wCtrl
        asideOutBuf := asideOutWire
        when(io.aside.in.rrdy){
          processLoad(asideOutWire)
        }.otherwise{
          exstat := LOAD_BLOCK
        }
      }
      is(tp.store){
        asideOutBuf := asideOutWire
        when(io.aside.in.wrdy){
          processStore(asideOutWire)
        }.otherwise{
          exstat := STORE_BLOCK
        }
      }
    }
  }

  val loadWctrlBuf = RegInit(initExeOut)
  def fillLoadSlot(nstat:Type) = {
    when(decode.req){
      // Check if collision exists
      when(
        decode.readInfo.reg_1.addr === loadWctrlBuf.bits.addr ||
          decode.readInfo.reg_2.addr === loadWctrlBuf.bits.addr ||
          decode.bits.wCtrl.addr === loadWctrlBuf.bits.addr
      ){
        // Do nothing
      }.otherwise{
        switch(decode.bits.exeOp.opType){
          is(tp.arith,tp.shift,tp.logic){
            io.pipe.decode.out.ack := true.B
            processComp()
          }
          is(tp.branch){
            io.pipe.decode.out.ack := true.B
            processBranch()
          }
          is(tp.jump){
            io.pipe.decode.out.ack := true.B
            processJump()
          }
        }
      }
    }
    exstat := nstat
  }
  switch(exstat){
    is(IDLE){
      default()
      when(decode.req){
        io.pipe.decode.out.ack := true.B
        exePathSelect()
      }
    }
    is(LOAD_BLOCK){
      default()
      when(io.aside.in.rrdy){
        processLoad(asideOutBuf)
      }.otherwise{
        fillLoadSlot(LOAD_BLOCK)
      }
    }
    is(LOADING){
      default()
      when(io.aside.in.rvalid){
        exstat := IDLE
        pipeOutReg := loadWctrlBuf
        pipeOutReg.bits.data := selByteInWords(asideOutBuf.byteSelN,io.aside.in.rdata)
        store2PreRes(true.B,loadWctrlBuf.bits.addr,selByteInWords(asideOutBuf.byteSelN,io.aside.in.rdata))
      }.otherwise{
        exstat := LOADING
      }
    }
    is(STORE_BLOCK){
      default()
      when(io.aside.in.wrdy){
        processStore(asideOutBuf)
      }.otherwise{
        exstat := STORE_BLOCK
      }
    }
  }
}
