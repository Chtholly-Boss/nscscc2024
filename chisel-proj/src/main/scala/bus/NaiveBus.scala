package bus
import chisel3.util._
import chisel3._
import pipeline.fetch.FetchPorts.{FetchAsideIn, FetchAsideOut}
import bus.sram.SramUtils.{sramInit,sramReadWord}
import bus.sram.SramPorts.{SramRequest,SramResponse}

class NaiveBus extends Module {
  val io = IO(new Bundle() {
    val in = Input(new FetchAsideOut)
    val out = Output(new FetchAsideIn)
    val sram = new Bundle {
      val in = Input(new SramResponse)
      val out = Output(new SramRequest)
    }
  })
  val initOut = Wire(new FetchAsideIn)
  initOut.rvalid := false.B
  initOut.rrdy := true.B
  initOut.inst := 0.U
  io.out := initOut

  io.sram.out := sramInit

  val outReg = RegInit(initOut)
  io.out := outReg
  val sramReqReg = RegInit(sramInit)
  io.sram.out := sramReqReg

  object State extends ChiselEnum {
    val IDLE,READ = Value
  }
  import State._

  val stat = RegInit(IDLE)
  switch (stat) {
    is (IDLE) {
      when (io.in.req) {
        stat := READ
        sramReqReg := sramReadWord(io.in.pc >> 2.U)
        outReg.rrdy := false.B
        outReg.rvalid := false.B
      } .otherwise {
        outReg.rrdy := true.B
        outReg.rvalid := false.B
      }
    }
    is (READ) {
      stat := IDLE
      outReg.rrdy := true.B
      outReg.rvalid := true.B
      outReg.inst := io.sram.in.rData
    }
  }
}
