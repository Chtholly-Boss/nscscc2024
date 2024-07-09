package pipeline.decode
import chisel3._
import chisel3.util._
import DecodePorts._
import DecodeUtils._

class DecodeStage extends Module {
  val io = IO(new DecodeIo)

  val du = Module(new Du)
  val srcInfo = RegInit(defaultSrcInfo)
  du.io.in := srcInfo

  object State extends ChiselEnum {
    val IDLE,DECODE,MUXDATA,DONE = Value
  }
  import State._

  val stat = RegInit(IDLE)
  switch(stat) {
    is (IDLE) {
      when (io.in.fetch.req) {
        stat := DECODE
        srcInfo := io.in.fetch.bits

      } .otherwise {
      }
    }
    is (DECODE) {
      stat := DONE
    }
    is (MUXDATA) {
      stat := DONE
    }
    is (DONE) {
      when (io.in.ack) {
        stat := IDLE
      }
    }
  }
}