package caches.ibuffer
import chisel3._
import bus.ultra.UltraBusParams._
object IbufferParams {
  val dataWidth = iBandWidth
  val offsetWidth = iOffsetWidth
  val tagWidth = 22 - offsetWidth
  val instWidth = 32
}
