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


  // Data Channel Utils
  object DataLoadType extends ChiselEnum {
    val
      CACHE,
      UNCACHE
    = Value
  }
  object DataState extends ChiselEnum {
    val
      IDLE,             // 0
      B_WAIT,           // 1
      E_WAIT,           // 2
      B_LOAD_SINGLE,    // 3
      B_LOAD_LINE,      // 4
      E_LOAD_SINGLE,   // 5
      E_LOAD_LINE,     // 6
      B_STORE,          // 7
      E_STORE           // 8
    = Value
  }
  def initDataReq:DataReq = {
    val init = Wire(new DataReq)
    init.rreq := false.B
    init.rtype := 0.U // Load Word
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
