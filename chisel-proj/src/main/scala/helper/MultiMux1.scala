package helper

import chisel3._
import chisel3.util._

/**
 * - Usage: Select One Module among same Modules using one Valid Signal
 * @param length: Number of Modules
 * @param tFactory: Info to transfer
 * @param blankT: All Zero, when input is invalid,it will be set to blankT
 * @tparam T:type
 */
class MultiMux1[T <: Data](length: Int, tFactory: => T, blankT: => T) extends Module {
  val io = IO(new Bundle {
    val inputs = Input(Vec(length, Valid(tFactory)))
    val output = Valid(tFactory)
  })
  // * check if there exist valid input
  io.output.valid := io.inputs.map(_.valid).reduce(_ || _)
  // * Select Valid input and Set others be Blank
  val flatten = Wire(Vec(length, tFactory))
  for (i <- 0 until length) {
    flatten(i) := Mux(io.inputs(i).valid, io.inputs(i).bits, blankT)
  }
  // * Only One is valid,Using | to reduce
  io.output.bits := VecInit(flatten.map(_.asUInt)).reduceTree(_ | _).asTypeOf(tFactory)
}