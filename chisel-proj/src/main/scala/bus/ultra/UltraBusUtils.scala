package bus.ultra
import chisel3._
import bus.ultra.UltraBusPorts._
import bus.ultra.UltraBusParams._
object UltraBusUtils {
    def initInstReq:InstReq = {
      val init = Wire(new InstReq)
      init.rreq := 0.U
      init.pc := initPcAddr
      init
    }
    def initInstRspns:InstRspns = {
      val init = Wire(new InstRspns)
      init.inst := 0.U
      init.rvalid := false.B
      init.rrdy := false.B
      init
    }
}
