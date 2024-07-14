package bus.sram
import chisel3.util.experimental.loadMemoryFromFileInline
class BaseSram extends SramSim {
  val path = "./func/bintests/demo.txt"
  if (path.trim().nonEmpty) {
    loadMemoryFromFileInline(mem,path)
  }
}
