package ultra.pipeline.exe
import chisel3._
object UltraExeParams {
  val dataWidth = 32.W
  val typeWidth = 4.W
  val opWidth = 3.W

  object ExeType {
    val nop = 0.U
    val logic = 1.U
    val shift = 2.U
    val arith = 3.U
    val branch = 4.U
    val jump = 5.U
    val load = 6.U
    val store = 7.U
  }
  object Logic {
    val num = 4
    val nop = 0.U
    val or = 1.U
    val xor = 2.U
    val and = 3.U
  }
  object Shift{
    val num = 3
    val nop = 0.U
    val srl = 1.U
    val sll = 2.U
  }
  object Arithmetic {
    val num = 5
    val nop = 0.U
    val add = 1.U
    val sltu = 2.U
    val sub = 3.U
    val mul = 4.U
  }
  object Branch {
    val num = 6
    val nop = 0.U
    val bne = 1.U
    val beq = 2.U
    val bge = 3.U
    val b_ = 4.U
    val bl = 5.U
  }
  object JumpBranch {
    val num = 2
    val nop = 0.U
    val jirl = 1.U
  }
  object Load {
    val num = 3
    val nop = 0.U
    val ld_w = 1.U
    val ld_b = 2.U
  }
  object Store {
    val num = 3
    val nop = 0.U
    val st_w = 1.U
    val st_b = 2.U
  }
}
