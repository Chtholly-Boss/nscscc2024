package pipeline.decode
import chisel3._

object DecodeParam {
  val pcWidth = 32.W
  val instWidth = 32.W
  val immWidth = 32.W
  // Belows are Machine Codes of Instructions
  object OpWidth {
    val _2RI12 = 10.W
    val _2RI16 = 6.W
    val _2R = 22.W
    val _3R = 17.W
  }
  object _3R {
    val add_w   = "b0000_0000_0001_0000_0".U
    val sub_w   = "b0000_0000_0001_0001_0".U
    val and_w   = "b0000_0000_0001_0100_1".U
    val or_w    = "b0000_0000_0001_0101_0".U
    val xor_w   = "b0000_0000_0001_0101_1".U
    val srl_w   = "b0000_0000_0001_0111_1".U
    val slli_w  = "b0000_0000_0100_0000_1".U
    val srli_w  = "b0000_0000_0100_0100_1".U
    val mul_w   = "b0000_0000_0001_1100_0".U
  }
  object _2RI12 {
    val addi_w = "b0000_0010_10".U
    val sltui  = "b0000_0010_01".U
    val ori    = "b0000_0011_10".U
    val andi   ="b0000_0011_01".U
    val ld_b  = "b0010_1000_00".U
    val st_b  = "b0010_1001_00".U
    val ld_w  = "b0010_1000_10".U
    val st_w  = "b0010_1001_10".U
  }
  object _2RI16 {
    val bne  = "b010_111".U
    val beq  = "b010_110".U
    val bge  = "b011_001".U
    val b_   = "b010_100".U
    val bl   = "b010_101".U
    val jirl = "b010_011".U
  }
  object _special {
    val lu12i_w   = "b0001_010".U
    val pcaddu12i = "b0001_110".U
  }
}