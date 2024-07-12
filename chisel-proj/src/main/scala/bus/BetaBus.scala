package bus
import chisel3._
import chisel3.util._
import bus.BusPorts._
import bus.BusParams._
import bus.sram.SramUtils._
import pipeline.fetch.FetchUtils._
import pipeline.exe.ExeUtils._
import bus.uart._
/*
  BetaBus stands on the perspective of Requests
  theoretically,every req could access all the address space
  For Simplicity:
  1. InstReq only reads baseRam
  2. DataReq read/write baseRam/extRam/uart
 */
class BetaBus extends Module {
  val io = IO(new Bundle() {
    val instChannel = new InstChannel
    val dataChannel = new DataChannel
    val baseRam = new SramChannel
    val extRam = new SramChannel
    val uart = new UartIo
  })
  // *********************************
  // Uart Signals
  val uartTransmitter = Module(new async_transmitter)
  val uartReceiver = Module(new async_receiver)
  val tData = Wire(UInt(8.W))
  val tStart = Wire(Bool())
  val rClear = Wire(Bool())
  val uStatus = Wire(UInt(2.W))
  tData := 0.U
  tStart := false.B
  rClear := false.B
  uStatus := uartReceiver.io.RxD_data_ready ## ~uartTransmitter.io.TxD_busy

  uartTransmitter.io.clk := clock
  io.uart.txd := uartTransmitter.io.TxD
  uartTransmitter.io.TxD_data := tData
  uartTransmitter.io.TxD_start := tStart

  uartReceiver.io.clk := clock
  uartReceiver.io.RxD := io.uart.rxd
  uartReceiver.io.RxD_clear := rClear
  // *********************************
  io.instChannel.rspns := initFetchAsideIn
  io.dataChannel.rspns := initExeAsideIn
  val instReg = RegInit(initFetchAsideIn)
  val dataReg = RegInit(initExeAsideIn)
  io.instChannel.rspns := instReg
  io.dataChannel.rspns := dataReg

  io.baseRam.req := initSramReq
  io.extRam.req := initSramReq
  val boutReg = RegInit(initSramReq)
  val eoutReg = RegInit(initSramReq)
  io.baseRam.req := boutReg
  io.extRam.req := eoutReg

  val instHasReq = WireDefault(io.instChannel.req.req)
  val dataHasReq = WireDefault(io.dataChannel.req.rreq | io.dataChannel.req.wreq)

  object IS extends ChiselEnum {
    val
      IDLE,
      WAIT,
      B_RD
    = Value
  }
  val istat = RegInit(IS.IDLE)
  val instReqBuf = RegInit(initFetchAsideOut)
  instReg.rrdy := istat === IS.IDLE

  object DS extends ChiselEnum {
    val
      IDLE,
      B_WAIT,
      E_WAIT,
      B_RD,
      B_WR,
      E_RD,
      E_WR,
      U_CS, // Check Status
      U_RD,
      U_WR
    = Value
  }
  val dstat = RegInit(DS.IDLE)
  val dataReqBuf = RegInit(initExeAsideOut)
  dataReg.rrdy := dstat === DS.IDLE
  dataReg.wrdy := dstat === DS.IDLE

  val baseRamBusy = Wire(Bool())
  val extRamBusy = Wire(Bool())
  baseRamBusy := istat === IS.B_RD || dstat === DS.B_RD || dstat === DS.B_WR
  extRamBusy := dstat === DS.E_RD || dstat === DS.E_WR

  // Inst req State Machine
  switch(istat) {
    is (IS.IDLE) {
      when(instHasReq) {
        // inst channel only access baseram
        instReqBuf := io.instChannel.req
        when(dataHasReq && io.dataChannel.req.addr(31,22) === SpaceMap.baseRamHighBits) {
          istat := IS.WAIT
        } .elsewhen(baseRamBusy) {
          istat := IS.WAIT
        } .otherwise {
          istat := IS.B_RD
          boutReg := sramReadWord(io.instChannel.req.pc(21,2))
        }
      }
      instReg.rvalid := false.B
    }
    is (IS.WAIT) {
      when(!baseRamBusy) {
        istat := IS.B_RD
        boutReg := sramReadWord(instReqBuf.pc(21,2))
      }
    }
    is (IS.B_RD) {
      // Assume that read in 1 cycle
      istat := IS.IDLE
      instReg.rvalid := true.B
      instReg.inst := io.baseRam.rspns.rData
    }
  }
  // Data Req State Machine
  switch(dstat) {
    is (DS.IDLE) {
      dataReg.rvalid := false.B
      dataReg.wdone := false.B
      rClear := false.B
      tStart := false.B
      when(dataHasReq) {
        dataReqBuf := io.dataChannel.req
        when(io.dataChannel.req.addr === SpaceMap.uartStatusAddr) {
          dstat := DS.U_CS
        }
        when(io.dataChannel.req.addr === SpaceMap.uartDataAddr) {
          when(io.dataChannel.req.rreq) {
            dstat := DS.U_RD
          }
          when(io.dataChannel.req.wreq) {
            dstat := DS.U_WR
          }
        }
        when(io.dataChannel.req.addr(31,22) === SpaceMap.baseRamHighBits) {
          when(baseRamBusy) {
            dstat := DS.B_WAIT
          }.otherwise {
            when(io.dataChannel.req.rreq) {
              dstat := DS.B_RD
              boutReg := sramRead(io.dataChannel.req.addr(21,2),io.dataChannel.req.byteSelN)
            }
            when(io.dataChannel.req.wreq) {
              dstat := DS.B_WR
              boutReg := sramWrite(
                io.dataChannel.req.addr(21,2),
                io.dataChannel.req.wdata,
                io.dataChannel.req.byteSelN
              )
            }
          }
        }
        when(io.dataChannel.req.addr(31,22) === SpaceMap.extRamHighBits) {
          when(extRamBusy) {
            dstat := DS.E_WAIT
          }.otherwise {
            when(io.dataChannel.req.rreq) {
              dstat := DS.E_RD
              eoutReg := sramRead(io.dataChannel.req.addr(21,2),io.dataChannel.req.byteSelN)
            }
            when(io.dataChannel.req.wreq) {
              dstat := DS.E_WR
              eoutReg := sramWrite(
                io.dataChannel.req.addr(21,2),
                io.dataChannel.req.wdata,
                io.dataChannel.req.byteSelN
              )
            }
          }
        }
      }
    }
    is (DS.B_WAIT) {
      when(!baseRamBusy){
        when(dataReqBuf.rreq) {
          dstat := DS.B_RD
          boutReg := sramRead(dataReqBuf.addr(21,2),dataReqBuf.byteSelN)
        }
        when(dataReqBuf.wreq) {
          dstat := DS.B_WR
          boutReg := sramWrite(
            dataReqBuf.addr(21,2),
            dataReqBuf.wdata,
            dataReqBuf.byteSelN
          )
        }
      }
    }
    is (DS.B_RD) {
      dstat := DS.IDLE
      dataReg.rvalid := true.B
      dataReg.rdata := io.baseRam.rspns.rData
    }
    is (DS.B_WR) {
      dstat := DS.IDLE
      dataReg.wdone := true.B
    }
    is (DS.E_WAIT) {
      when(!extRamBusy){
        when(dataReqBuf.rreq) {
          dstat := DS.E_RD
          eoutReg := sramRead(dataReqBuf.addr(21,2),dataReqBuf.byteSelN)
        }
        when(dataReqBuf.wreq) {
          dstat := DS.E_WR
          eoutReg := sramWrite(
            dataReqBuf.addr(21,2),
            dataReqBuf.wdata,
            dataReqBuf.byteSelN
          )
        }
      }
    }
    is (DS.E_RD) {
      dstat := DS.IDLE
      dataReg.rvalid := true.B
      dataReg.rdata := io.extRam.rspns.rData
    }
    is (DS.E_WR) {
      dstat := DS.IDLE
      dataReg.wdone := true.B
    }
    is (DS.U_CS) {
      dstat := DS.IDLE
      dataReg.rvalid := true.B
      dataReg.rdata := 0.U(30.W) ## uStatus
    }
    is (DS.U_RD) {
      dstat := DS.IDLE
      dataReg.rvalid := true.B
      dataReg.rdata := 0.U(24.W) ## uartReceiver.io.RxD_data
      rClear := true.B
    }
    is (DS.U_WR) {
      dstat := DS.IDLE
      dataReg.wdone := true.B
      tStart := true.B
      tData := dataReqBuf.wdata(7,0)
    }
  }

}
