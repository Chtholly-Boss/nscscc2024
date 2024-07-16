package ultra.caches.icache

import chisel3._
object IcacheUtils {
  object IcacheState extends ChiselEnum {
    val
      IDLE,
      TAG_CHECK,
      SEND,
      WAIT
    = Value
  }
}

