package ultra.helper
import chisel3._
import chisel3.util._

object FuncTemp {
  /**
   * This Template describe a single input function unit
   * ex:
   * * sqrt
   * * bit-operation
   */
  class SingleInput extends Module {
    val io = IO(new Bundle {
      val in = Input(new Bundle {
        val start = Bool()
        val operand = UInt(32.W)
      })
      val out = Output(new Bundle {
        val res = UInt(32.W)
        val done = Bool()
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

    // Rename the buf as the concrete definition like dividend/divisor
    val opBuf = RegInit(0.U(32.W))
    // Calculation Logic Here
    /**
     * Pseudo code here (python description)
     */



    // Implementation Here

    // State Machine
    val stat = RegInit(IDLE)
    switch(stat){
      is(IDLE){
        when(io.in.start){
          // Cache the Input here
          opBuf := io.in.operand
          stat := CALC
        }
      }
      is(CALC){

      }
      is(DONE){

        io.out.done := true.B
      }
    }
  }

  /**
   * This Template describe a double input function unit
   * ex:
   * * div/mod
   * * power
   */
  class DoubleInput extends Module {
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

    // Rename the buf as the concrete definition like dividend/divisor
    val opLeftBuf = RegInit(0.U(32.W))
    val opRightBuf = RegInit(0.U(32.W))
    // Calculation Logic Here
    /**
     * Pseudo code here (python description)
     */



    // Implementation Here

    // State Machine
    val stat = RegInit(IDLE)
    switch(stat){
      is(IDLE){
        when(io.in.start){
          // Cache the Input here
          opLeftBuf := io.in.opLeft
          opRightBuf := io.in.opRight
          stat := CALC
        }
      }
      is(CALC){

      }
      is(DONE){
        io.out.done := true.B
      }
    }
  }
}
