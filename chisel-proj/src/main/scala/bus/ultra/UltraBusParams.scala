package bus.ultra
import chisel3._
import chisel3.util._
object UltraBusParams {
  val wordLength = 32
  val iWords = 8
  val iOffsetWidth = 2 + log2Ceil(iWords)
  val dWords = 4
  val dOffsetWidth = 2 + log2Ceil(dWords)
  val iBandWidth = iWords * wordLength
  val dBandWidth = dWords * wordLength
  val _M = math.pow(10,6).toInt
  val cpuFrequency = 100 * _M
  val sramFrequency = 60 * _M
  val memCycles = 2

  val initPcAddr = "h8000_0000".U
  // Sram Virtual Addr(31,22)
  val baseSramAddr = "b1000_0000_00".U
  val extSramAddr = "b1000_0000_01".U
  // Kernel Working Space and User Working Space
  // Program Addr: Virtual Addr(21,20)
  val coreProgAddr = "b00".U
  val usrProgAddr = "b01".U // 01,10,11 are all user program space
  // Data Addr: Virtual Addr(21,16)
  val usrDataAddr = "b00_0000".U
  val coreDataAddr = "b11_1111".U

  // Uart Virtual Addr
  // For simplicity,Check (31,24) is enough
  val uartAddr = "hbf".U
  // Status and Data Addr:Virtual Addr(3,0)
  val uartStatAddr = "hc".U
  val uartDataAddr = "h8".U
}
