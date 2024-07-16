package ultra.caches.ibuffer

import chisel3._
import ultra.bus.UltraBusParams._
object IbufferParams {
  val dataWidth = iBandWidth
  val offsetWidth = iOffsetWidth
  val tagWidth = 22 - offsetWidth
  val instWidth = 32
  val validBit = tagWidth
  val bandWidth = iBandWidth
}
