package ultra.bus
import chisel3._
import UltraBusParams._
import sram.SramPorts._
object UltraBusPorts {
  // --- Instruction Channel ---
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

  // --- Data Channel ---
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
  class DataMasterIo extends Bundle {
    val out = Output(new DataReq)
    val in = Input(new DataRspns)
  }
  class DataSlaveIo extends Bundle {
    val in = Input(new DataReq)
    val out = Output(new DataRspns)
  }

  // --- Sram Channel ---
  class SramMasterIo extends Bundle {
    val in = Input(new SramResponse)
    val out = Output(new SramRequest)
  }
  // --- Uart Channel ---
  class UartMasterIo extends Bundle {
    val txd = Output(UInt(1.W))
    val rxd = Input(UInt(1.W))
  }
}
