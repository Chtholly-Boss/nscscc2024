package ultra.bus
import chisel3._
import chisel3.util._
object UltraBusParams {
  val wordLength = 32
  val iWords = 4
  val iOffsetWidth = 2 + log2Ceil(iWords)
  val dWords = 1
  val dOffsetWidth = 2 + log2Ceil(dWords)
  val iBandWidth = iWords * wordLength
  val dBandWidth = dWords * wordLength
  val _M = math.pow(10,6).toInt
  val cpuFrequency = 160 * _M
  val sramFrequency = 60 * _M
  val memCycles = 3
  val mulCycles = 1

  val initPcAddr = "h8000_0000".U
  // Sram Virtual Addr(31,22)
  val baseSramAddr = "b00".U
  val extSramAddr = "b01".U
  // Kernel Working Space and User Working Space
  // Program Addr: Virtual Addr(21,20)
  val coreProgAddr = "b00".U
  val usrProgAddr = "b01".U // 01,10,11 are all user program space
  // Data Addr: Virtual Addr(21,16)
  val usrDataAddr = "b00_0000".U
  val coreDataAddr = "b11_1111".U

  // Uart Virtual Addr
  // For simplicity,Check (31,24) is enough
  val uartAddr = "b11".U
  // Status and Data Addr:Virtual Addr(3,0)
  val uartStatAddr = "hc".U
  val uartDataAddr = "h8".U
}
