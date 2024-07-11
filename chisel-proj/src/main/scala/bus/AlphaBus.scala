package bus
import chisel3._
import chisel3.util._
import bus.BusPorts._
import bus.BusParams._
import bus.sram.SramUtils._
import pipeline.fetch.FetchUtils._
import pipeline.exe.ExeUtils._

// AlphaBus stand from the perspective of SRAM
// Inst could only Read BaseRam
// Data cound only Read/Write ExtRam
class AlphaBus extends Module {
  val io = IO(new Bundle() {
    val instChannel = new InstChannel
    val dataChannel = new DataChannel
    val baseRam = new SramChannel
    val extRam = new SramChannel
  })
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
  /*
  State Machine Spec:
    1. has request? Check the address
    2. Process the request
   */
  // BaseRam State Machine
  val instReq = RegInit(initFetchAsideOut)
  val dataReq = RegInit(initExeAsideOut)
  when (instHasReq) {
    instReq := io.instChannel.req
  }
  when (dataHasReq) {
    dataReq := io.dataChannel.req
  }

  object BS extends ChiselEnum {
    val IDLE,PROCESS,READ = Value
  }
  val bstat = RegInit(BS.IDLE)
  instReg.rrdy := bstat === BS.IDLE
  switch (bstat) {
    is (BS.IDLE) {
      when (instHasReq) {
        when (io.instChannel.req.pc(31,22) === SpaceMap.baseRamHighBits) {
          bstat := BS.PROCESS
        }
      }
      instReg.rvalid := false.B
    }
    is (BS.PROCESS) {
      when (instReq.req) {
        bstat := BS.READ
        boutReg := sramReadWord(instReq.pc(21,2))
        instReg.rvalid := false.B
      }
    }
    is (BS.READ) {
      bstat := BS.IDLE
      instReg.rvalid := true.B
      instReg.inst := io.baseRam.rspns.rData
    }
  }

  // ExtRam State Machine
  object ES extends ChiselEnum {
    val IDLE,PROCESS,READ,WRITE = Value
  }
  val estat = RegInit(ES.IDLE)
  dataReg.rrdy := estat === ES.IDLE
  dataReg.wrdy := estat === ES.IDLE
  switch (estat) {
    is (ES.IDLE) {
      when (dataHasReq) {
        when (io.dataChannel.req.addr(31,22) === SpaceMap.extRamHighBits) {
          estat := ES.PROCESS
        }
      }
      dataReg.rvalid := false.B
      dataReg.wdone := false.B
    }
    is (ES.PROCESS) {
      when (dataReq.rreq) {
        estat := ES.READ
        eoutReg := sramReadWord(dataReq.addr(21,2))
        dataReg.rvalid := false.B
      }

      when (dataReq.wreq) {
        estat := ES.WRITE
        eoutReg := sramWrite(dataReq.addr(21,2),dataReq.wdata)
        dataReg.wdone := false.B
      }
    }
    is (ES.READ) {
      estat := ES.IDLE
      dataReg.rdata := io.extRam.rspns.rData
      dataReg.rvalid := true.B
    }
    is (ES.WRITE) {
      estat := ES.IDLE
      dataReg.wdone := true.B
    }
  }
}