package ultra.caches.icache
import ultra.bus.UltraBusParams._
object IcacheParams {
  // Direct Mapping
  val words = iWords
  val bandwidth = iBandWidth
  val iRomAddrWidth = 22
  val offsetWidth = iOffsetWidth
  val indexWidth = 6
  val tagWidth = iRomAddrWidth - offsetWidth - indexWidth
  val validBit = tagWidth
  val depth = 1 << indexWidth
}
