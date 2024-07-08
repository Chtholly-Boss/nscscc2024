package bus.sram
import chisel3.util.experimental.loadMemoryFromFileInline
class BaseSram extends SramSim {
  val path = "./func/bintests/simpleTest.txt"
  if (path.trim().nonEmpty) {
    loadMemoryFromFileInline(mem,path)
  }
}
