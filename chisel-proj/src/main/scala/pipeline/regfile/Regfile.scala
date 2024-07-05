package pipeline.regfile
import chisel3._

import RegfilePorts._
import RegfileParam._

class Regfile extends Module {
  val io = IO(new RegfileIo)
  val regs = RegInit(VecInit(Seq.fill(32)(0.U(dataWidth))))
  // rChannel 1
  when (io.rChannel_1.in.en) {
    when (io.rChannel_1.in.addr === 0.U) {
      io.rChannel_1.out := 0.U
    } .elsewhen(
      io.rChannel_1.in.addr === io.wChannel.addr
      && io.wChannel.en
    ) {
      io.rChannel_1.out := io.wChannel.data
    } .otherwise {
      io.rChannel_1.out := regs(io.rChannel_1.in.addr)
    }
  }
  when (io.rChannel_2.in.en) {
    when (io.rChannel_2.in.addr === 0.U) {
      io.rChannel_2.out := 0.U
    } .elsewhen(
      io.rChannel_2.in.addr === io.wChannel.addr
        && io.wChannel.en
    ) {
      io.rChannel_2.out := io.wChannel.data
    } .otherwise {
      io.rChannel_2.out := regs(io.rChannel_2.in.addr)
    }
  }
}
