package bus.ultra
import chisel3._
import bus.ultra.UltraBusParams._
object UltraBusPorts {
  // Instruction Channel
  class InstReq extends Bundle {
    val pc = UInt(wordLength.W)
    val rreq = Bool()
  }

  class InstRspns extends Bundle {
    val rdata = UInt(iBandWidth.W)
    val rrdy = Bool()
    val rvalid = Bool()
  }

  class InstMasterIo extends Bundle {
    val out = Output(new InstReq)
    val in = Input(new InstRspns)
  }
  class InstSlaveIo extends Bundle {
    val in = Input(new InstReq)
    val out = Output(new InstRspns)
  }
  // Data Channel
  class DataReq extends Bundle {
    val rreq = Bool()
    val wreq = Bool()
    // byteSelN will only be used in Write Operation
    // When LoadBytes,the core will choose bytes itself
    val byteSelN = UInt(4.W)
    val addr = UInt(wordLength.W)
    val wdata = UInt(wordLength.W)
  }

  class DataRspns extends Bundle {
    val rdata = UInt(dBandWidth.W)
    val rrdy = Bool()
    val rvalid = Bool()
    val wrdy = Bool()
    val wdone = Bool()
  }
}
