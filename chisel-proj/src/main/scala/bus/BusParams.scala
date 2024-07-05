package bus
import chisel3._
object BusParams {
  object SpaceMap {
    // Address Map of Virtual Address Space
    val baseRamB = "h8000_0000".U
    val baseRamE = "h803F_FFFF".U
    val extRamB = "h8040_0000".U
    val extRamE = "h807F_FFFF".U
    val uartDataAddr = "hbfd0_03f8".U
    val uartStatusAddr = "hbfd0_03fc".U
  }
  object Uart {
    val frequency: Int = 50 * math.pow(10,6).toInt
    val baudRate = 115200
  }
}