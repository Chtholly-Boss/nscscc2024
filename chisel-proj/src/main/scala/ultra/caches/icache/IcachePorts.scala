package ultra.caches.icache

import chisel3._
import ultra.bus.UltraBusPorts._
object IcachePorts {
  class IcacheIo extends Bundle {
    val core = new InstSlaveIo
    val bus = new InstMasterIo
  }
}
