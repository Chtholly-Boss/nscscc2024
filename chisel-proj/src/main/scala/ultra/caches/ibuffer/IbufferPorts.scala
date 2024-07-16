package ultra.caches.ibuffer

import chisel3._
import IbufferParams._
import ultra.bus.UltraBusPorts._

object IbufferPorts {
  class InstOut extends Bundle{
    val inst = UInt(instWidth.W)
    val rvalid = Bool() // Updated
  }
  class IbufferUnitIo extends Bundle {
    val in = Input(new Bundle {
      val pc = UInt(32.W)
      val index = UInt(offsetWidth.W)
      val rvalid = Bool()
      val inst = UInt(instWidth.W)
    })
    val out = Output(new Bundle {
      val isMatched = Bool()
      val bits = new InstOut
    })
  }
  class IbufferIo extends Bundle {
    val icache = new Bundle {
      val in = Input(new InstRspns)
      val in_pc = Input(UInt(32.W))
    }
    val core = new Bundle {
      val in = Input(new Bundle() {
        val pc = UInt(32.W)
      })
      val out = Output(new Bundle {
        val hit = Bool()
        val bits = new InstOut
      })
    }
  }
}
