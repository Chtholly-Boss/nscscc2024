package ultra.bus.sram

import chisel3.util.experimental.loadMemoryFromFileInline
class ExtSram(path:String) extends SramSim {
  if (path.trim().nonEmpty) {
    loadMemoryFromFileInline(mem,path)
  }
}
