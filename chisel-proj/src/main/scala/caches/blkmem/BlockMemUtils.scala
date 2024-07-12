package caches.blkmem
import chisel3._
import chisel3.util._

object BlockMemUtils {

  val _KB = 1 << 10
  val _MB = 1 << 20

  val wordLength = 32
  val instMemSize = 1 << 22
  val dataMemSize = 1 << 23
  val instMemAddrWidth = 22
  val dataMemAddrWidth = 23

  class ICacheParams(size:Int,wordsPerLine:Int) {
    val offsetWidth = log2Ceil(wordsPerLine)
    val depth = size / wordsPerLine // No groups
    val addrWidth = log2Ceil(depth)
    val dataWidth = wordLength * wordsPerLine
    val tagWidth = 32 - offsetWidth - addrWidth
  }
  val _8KB_4WordsPerLine = new ICacheParams(8 * _KB, 4)
  val _32KB_8WordsPerLine = new ICacheParams(32 * _KB, 8)
}
