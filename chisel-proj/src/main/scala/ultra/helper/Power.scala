package ultra.helper
import chisel3._
import chisel3.util._

class Power extends Module {
  val io = IO(new Bundle {
    val in = Input(new Bundle() {
      val start = Bool()
      val opLeft = UInt(32.W)
      val opRight = UInt(32.W)
    })
    val out = Output(new Bundle {
      val done = Bool()
      val res = UInt(32.W)
    })
  })

  io.out.res := 0.U
  io.out.done := false.B
  object State extends ChiselEnum{
    val
      IDLE,
      CALC,
      DONE
    = Value
  }
  import State._
  /**
   Binary Power From OI wiki
   * a0 = 1
   * while( a2 > 0 ):
   *     if a2 & 1:
   *         a0 = a0 * a1
   *     a1 = a1 * a1
   *     a2 = a2 >> 1
   */
  val base = RegInit(0.U(32.W))
  val power = RegInit(0.U(32.W))

  val stat = RegInit(IDLE)
  // - Calculation
  val res = RegInit(1.U(32.W)) // res = 1
  val isPowerGtZero = WireDefault(power.asSInt >  0.S) // a2 > 0
  val isPowerAnd1 = WireDefault((power & 1.U) =/= 0.U) // a2 & 1
  // State Machine
  switch(stat){
    is(IDLE){
      when(io.in.start){
        // buf the input
        base := io.in.opLeft
        power := io.in.opRight
        stat := CALC
      }
    }
    is(CALC){
      when(isPowerGtZero){
        when(isPowerAnd1){
          res := (res.asSInt * base.asSInt).asUInt
        }
        base := (base.asSInt * base.asSInt).asUInt
        power := power >> 1.U
      }.otherwise{
        stat := DONE
      }
    }
    is(DONE){
      io.out.done := true.B
      io.out.res := res
      stat := IDLE
      res := 1.U
    }
  }
}
