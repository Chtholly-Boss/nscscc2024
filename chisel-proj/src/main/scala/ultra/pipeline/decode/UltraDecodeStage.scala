package ultra.pipeline.decode
import chisel3._
import chisel3.util._
import UltraDecodePorts.DecodeIo
import UltraDecodeUtils._
import ultra.pipeline.exe.UltraExeParams.ExeType
class UltraDecodeStage extends Module {
  val io = IO(new DecodeIo)
  // Ports Signal Initialization
  io.pipe.fetch.out.ack := false.B
  val pipeOutReg = RegInit(initDecodeOut)
  io.pipe.exe.out := pipeOutReg
  val instIn = WireDefault(io.pipe.fetch.in.bits.inst)
  when(instIn(31,28) === "b0001".U){
    // Special Instruction
    io.aside.out.reg_1.addr := 0.U
  }.otherwise{
    io.aside.out.reg_1.addr := instIn(9,5)
  }
  when(instIn(31,24) === "h00".U){
    // 3R instruction
    io.aside.out.reg_2.addr := instIn(14,10)
  }.otherwise{
    io.aside.out.reg_2.addr := instIn(4,0)
  }
  // Internal Utils
  val du = Module(new Du)
  du.io.in := io.pipe.fetch.in.bits
  object DecodeState extends ChiselEnum {
    val
      IDLE,
      DONE
    = Value
  }
  import DecodeState._
  val dcstat = RegInit(IDLE)
  def default():Unit = {
    dcstat := IDLE
    pipeOutReg := initDecodeOut
    io.pipe.fetch.out.ack := false.B
  }
  def getDecodeRes():Unit = {
    default()
    dcstat := DONE
    io.pipe.fetch.out.ack := true.B
    pipeOutReg.req := true.B
    pipeOutReg.fetchInfo := io.pipe.fetch.in.bits
    pipeOutReg.readInfo.reg_1 := du.io.out.bits.reg_1
    pipeOutReg.readInfo.reg_2 := du.io.out.bits.reg_2
    pipeOutReg.bits.exeOp := du.io.out.bits.exeOp
    pipeOutReg.bits.wCtrl := du.io.out.bits.wCtrl
    pipeOutReg.bits.operands.hasImm := du.io.out.bits.hasImm
    pipeOutReg.bits.operands.imm := du.io.out.bits.imm
    pipeOutReg.bits.operands.regData_1 := io.aside.in.regLeft
    pipeOutReg.bits.operands.regData_2 := io.aside.in.regRight
  }

  when(io.pipe.br.isMispredict){
    default()
  }.otherwise{
    switch(dcstat){
      is (IDLE){
        when(io.pipe.fetch.in.req){
          getDecodeRes()
        }.otherwise{
          default()
        }
      }
      is (DONE){
        when(io.pipe.exe.in.ack){
          when(io.pipe.fetch.in.req){
            getDecodeRes()
          }.otherwise{
            default()
          }
        }.otherwise{
          io.aside.out.reg_1 := pipeOutReg.readInfo.reg_1
          io.aside.out.reg_2 := pipeOutReg.readInfo.reg_2
          pipeOutReg.bits.operands.regData_1 := io.aside.in.regLeft
          pipeOutReg.bits.operands.regData_2 := io.aside.in.regRight
        }
      }
    }
  }
}
