package ultra.caches.bpbuffer

import ultra.bus.UltraBusPorts.InstRspns
import chisel3._
import ultra.caches.icache.IcacheParams._
object BpBufferPorts {
  class BpOut extends Bundle {
    val predictTaken = Bool()
    val offset = UInt(32.W)
  }
  class BpUnitIo extends Bundle {
    val in = Input(new Bundle {
      val pc = UInt(32.W)
      val index = UInt(offsetWidth.W)
      val rvalid = Bool()
      val inst = UInt(32.W)
    })
    val out = Output(new Bundle {
      val isMatched = Bool()
      val bits = new BpOut
    })
  }
  class BpBufferIo extends Bundle {
    val icache = new Bundle {
      val in = Input(new InstRspns)
    }
    val core = new Bundle {
      val in = Input(new Bundle {
        val pc = UInt(32.W)       // For Selecting the outputs
      })
      val out = Output(new BpOut)
    }
  }
}
