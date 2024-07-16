package ultra.caches.dcache

import chisel3._
import ultra.bus.UltraBusPorts._
object DcachePorts {
  class DcacheIo extends Bundle {
    val core = new DataSlaveIo
    val bus = new DataMasterIo
  }
}
