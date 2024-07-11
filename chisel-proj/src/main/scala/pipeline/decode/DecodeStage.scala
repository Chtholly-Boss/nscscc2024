package pipeline.decode
import chisel3._
import chisel3.util._
import DecodePorts._
import DecodeUtils._

class DecodeStage extends Module {
  val io = IO(new DecodeIo)
  io.out.ack := false.B
  io.out.decode := initDecodeOut
  val asideOut = RegInit(initDecodeAsideOut)
  io.aside.out := asideOut
  val decodeOut = RegInit(initDecodeOut)
  io.out.decode := decodeOut

  val du = Module(new Du)
  val srcInfo = RegInit(initDecodeSrcInfo)
  du.io.in := srcInfo


  object State extends ChiselEnum {
    val IDLE,DECODE,READ,DONE = Value
  }
  import State._

  val stat = RegInit(IDLE)
  when (io.bCtrl.isMispredict) {
    stat := IDLE
    decodeOut := initDecodeOut
    asideOut := initDecodeAsideOut
  } .otherwise {
    switch(stat) {
      is (IDLE) {
        when (io.in.fetch.req) {
          // Cache the inputs
          stat := DECODE
          srcInfo := io.in.fetch.bits
          // ack the req
          io.out.ack := true.B
        } .otherwise {
          stat := IDLE
          srcInfo := initDecodeSrcInfo
          io.out.ack := false.B
        }
      }
      is (DECODE) {
        stat := READ
        // Send read req to regfile
        when (du.io.out.isMatched) {
          asideOut.reg_1 := du.io.out.bits.reg_1
          asideOut.reg_2 := du.io.out.bits.reg_2
        }
      }
      is (READ) {
        stat := DONE
        // Send Decode Res
        when (du.io.out.isMatched) {
          decodeOut.req := true.B
          decodeOut.bits.exeOp := du.io.out.bits.exeOp
          decodeOut.bits.wCtrl := du.io.out.bits.wCtrl
          decodeOut.bits.operands.hasImm := du.io.out.bits.hasImm
          decodeOut.bits.operands.imm := du.io.out.bits.imm
          decodeOut.bits.operands.regData_1 := io.aside.in.regLeft
          decodeOut.bits.operands.regData_2 := io.aside.in.regRight
          decodeOut.fetchInfo := srcInfo
        }
      }
      is (DONE) {
        when (io.in.ack) {
          stat := IDLE
          decodeOut := initDecodeOut
        }
      }
    }
  }
}