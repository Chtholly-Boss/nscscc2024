package ultra.bus
import chisel3._
import UltraBusPorts._
import UltraBusParams._

object UltraBusUtils {
  // Instruction Channel Utils
  object InstState extends ChiselEnum {
    val
      IDLE,   // 0
      WAIT,   // 1
      B_LOAD  // 2
    = Value
  }

  def initInstReq:InstReq = {
    val init = Wire(new InstReq)
    init.rreq := 0.U
    init.pc := initPcAddr
    init
  }
  def initInstRspns:InstRspns = {
    val init = Wire(new InstRspns)
    init.rdata := 0.U
    init.rvalid := false.B
    init.rrdy := true.B
    init
  }

  object DataState extends ChiselEnum {
    val
      IDLE,
      B_WAIT,
      E_WAIT,
      B_LOAD,
      E_LOAD,
      B_STORE,
      U_CHECK,
      U_LOAD,
      U_STORE,
      E_STORE
    = Value
  }
  def initDataReq:DataReq = {
    val init = Wire(new DataReq)
    init.rreq := false.B
    init.wreq := false.B
    init.wdata := 0.U
    init.addr := 0.U
    init.byteSelN := "b0000".U
    init
  }
  def initDataRspns:DataRspns = {
    val init = Wire(new DataRspns)
    init.rrdy := true.B
    init.rvalid := false.B
    init.rdata := 0.U
    init.wdone := false.B
    init.wrdy := true.B
    init
  }
}
