package bus.sram
import chisel3.util.experimental.loadMemoryFromFileInline
class ExtSram extends SramSim {
  val path = "./func/bintests/matrix.txt"
  if (path.trim().nonEmpty) {
    loadMemoryFromFileInline(mem,path)
  }
}
