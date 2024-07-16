package ultra.bus.sram

import chisel3.util.experimental.loadMemoryFromFileInline
class BaseSram(path:String) extends SramSim {
  if (path.trim().nonEmpty) {
    loadMemoryFromFileInline(mem,path)
  }
}
