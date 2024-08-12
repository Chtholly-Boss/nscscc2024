package ultra.pipeline.exe
import chisel3._
import chisel3.util._
class DivU extends Module {
  val io = IO(new Bundle {
    val in = Input(Valid(
      new Bundle() {
        val left = UInt(32.W)
        val right = UInt(32.W)
      })
    )
    val out = Output(Valid(
      new Bundle() {
        val quotient = UInt(32.W)
        val remainder = UInt(32.W)
      })
    )
  })

  io.out.valid := false.B
  io.out.bits.quotient  := 0.U
  io.out.bits.remainder := 0.U
  // alias the io ports
  val dividend = io.in.bits.left
  val divisor = io.in.bits.right

  object State extends ChiselEnum {
    val FREE, CLZ, CALC = Value
  }
  import State._
  val stat = RegInit(FREE)
  val cycleRemaining = RegInit(0.U(31.W))

  def getAbs(data:Bits):Bits = {
    Mux(
      data(31) === 1.U,
      (~data).asUInt + 1.U,
      data
    )
  }

  val dividendReg = RegInit(0.U(32.W))
  val divisorReg = RegInit(0.U(32.W))
  val dividendAbsReg = RegInit(0.U(32.W))
  val divisorAbsReg = RegInit(0.U(32.W))
  val quotientSign = RegInit(0.U(1.W))
  val remainderSign = RegInit(0.U(1.W))
  val quotientReg = RegInit(0.U(32.W))
  val remainderReg = RegInit(0.U(32.W))


  val Clz_dividend = Module(new ClzU)
  val Clz_divisor = Module(new ClzU)
  Clz_dividend.io.input := dividendAbsReg
  Clz_divisor.io.input := divisorAbsReg
  val clzDelta = WireDefault(Clz_divisor.io.output - Clz_dividend.io.output)
  val shiftedDivisorReg = RegInit(0.U(32.W))

  // CALC relevant
  val sub2x = Wire(UInt((32 + 2).W))
  sub2x := remainderReg -& (shiftedDivisorReg << 1).asUInt // ! may have bit width problem

  // shiftDivider过大
  val sub2xOverflow = WireDefault(sub2x(32 + 2 - 1))
  val sub2xToss     = WireDefault(sub2x(32))
  val sub2xValue    = WireDefault(sub2x(31, 0))

  // 比较remainder和divisor移动偶数次数

  val sub1x = Wire(UInt((32 + 1).W))
  sub1x := Mux(
    sub2xOverflow, // remainder - 2 * divisor < 0
    Cat(sub2xToss, sub2xValue) + shiftedDivisorReg, // sub1x = remainder - shiftDivisor
    Cat(sub2xToss, sub2xValue) - shiftedDivisorReg // sub1x = remainder - 3 * shiftDivisor
  )

  val sub1xOverflow = WireDefault(sub1x(32))
  val sub1xValue    = WireDefault(sub1x(31, 0))

  val newQuotientBits = WireDefault(
    Cat(~sub2xOverflow, ~sub1xOverflow)
  )

  val cyclesRemainingSub1 = Wire(UInt(5.W))
  cyclesRemainingSub1 := cycleRemaining -& 1.U

  val isTerminateReg = RegInit(false.B)
  isTerminateReg := false.B

  switch(stat){
    is(FREE){
      when(io.in.valid){
        stat := CLZ
        dividendReg := dividend
        divisorReg := divisor
        dividendAbsReg := getAbs(dividend)
        divisorAbsReg := getAbs(divisor)
        quotientSign := dividend(31) ^ divisor(31)
        remainderSign := dividend(31)
      }
    }
    is(CLZ){
      when(divisorAbsReg > dividendAbsReg){
        stat := FREE
        io.out.valid := true.B
        io.out.bits.quotient := 0.U
        io.out.bits.remainder := divisorReg
      }.otherwise{
        stat := CALC
      }
      shiftedDivisorReg  := divisorAbsReg << Cat(clzDelta(5 - 1, 1), 0.U(1.W))
      remainderReg       := dividendAbsReg
      quotientReg        := 0.U(32.W)
      cycleRemaining := clzDelta(5 - 1, 1)
    }
    is(CALC){
      shiftedDivisorReg := shiftedDivisorReg >> 2
      remainderReg := Mux(
        !newQuotientBits.orR,
        remainderReg, // stay the same
        Mux(
          sub1xOverflow,
          sub2xValue, // sub2x = remainder - 2 * shiftDivisor
          sub1xValue // sub1x = remainder - (3 or 1) * shiftDivisor
        )
      )
      quotientReg        := Cat(quotientReg(32 - 3, 0), newQuotientBits)
      cycleRemaining := cyclesRemainingSub1(5 - 2, 0)
      isTerminateReg := cyclesRemainingSub1(4)
      when(isTerminateReg) {
        stat := FREE
        io.out.valid := true.B
        io.out.bits.quotient := Mux(
          quotientSign === 1.U,
          (~quotientReg).asUInt + 1.U,
          quotientReg
        )
        io.out.bits.remainder := Mux(
          remainderSign === 1.U,
          (~remainderReg).asUInt + 1.U,
          remainderReg
        )
      }
    }
  }
}
