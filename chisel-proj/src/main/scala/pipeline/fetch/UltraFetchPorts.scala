package pipeline.fetch

import chisel3._

object UltraFetchPorts {
  class UltraFetchAsideIn extends Bundle {
    val inst = UInt(32.W)
    val rvalid = Bool()
    val isInBuf = Bool()
    val pcOffset = UInt(32.W)
    val predictTaken = Bool()
  }
  class UltraFetchAsideOut extends Bundle {
    val pc = UInt(32.W)
    val rreq = Bool()
  }
  class UltraFetchAsideMasterIo extends Bundle{
    val out = Output(new UltraFetchAsideOut)
    val in = Input(new UltraFetchAsideIn)
  }
  class UltraFetchAsideSlaveIo extends Bundle{
    val in = Input(new UltraFetchAsideOut)
    val out = Output(new UltraFetchAsideIn)
  }
}
