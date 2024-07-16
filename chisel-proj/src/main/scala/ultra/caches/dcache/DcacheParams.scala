package ultra.caches.dcache
import ultra.bus.UltraBusParams._
object DcacheParams {
  // Direct Mapping
  // Index bits pick several from high bits
  val words = dWords
  val bandWidth = dBandWidth
  val dRamAddrWidth = 23
  val offsetWidth = dOffsetWidth
  val indexWidth = 6
  val indexHighBitWidth = 2
  val indexLowBitWidth = indexWidth - indexHighBitWidth
  val tagWidth = dRamAddrWidth - indexWidth - offsetWidth
  val validBit = tagWidth
  val depth = 1 << indexWidth
}
