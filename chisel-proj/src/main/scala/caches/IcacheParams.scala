package caches
import chisel3._
import chisel3.util._
object IcacheParams {
  val _KB = 1 << 10
  val _MB = 1 << 20

  val wordLength = 32
  val instMemSize = 1 << 22
  val instMemAddrWidth = 22

  class ParamsBundle(size:Int, wordsPerLine:Int) {
    val offsetWidth = log2Ceil(wordsPerLine)
    val depth = size / wordsPerLine // No groups
    val addrWidth = log2Ceil(depth)
    val dataWidth = wordLength * wordsPerLine
    val tagWidth = 32 - offsetWidth - addrWidth
  }
  val _8KB_4WordsPerLine = new ParamsBundle(8 * _KB, 4)
  val _32KB_8WordsPerLine = new ParamsBundle(32 * _KB, 8)
}
