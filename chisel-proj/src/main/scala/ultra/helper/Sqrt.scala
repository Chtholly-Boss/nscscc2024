import chisel3._
import chisel3.util._

class Sqrt extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(32.W))  // 输入的正整数
    val start = Input(Bool())    // 启动信号
    val out = Output(UInt(16.W))  // 输出的平方根向下取整
    val done = Output(Bool())     // 完成信号
  })
  val low = RegInit(1.U(16.W))      // 查找区间下限
  val high = RegInit(0.U(16.W))     // 查找区间上限
  val mid = Wire(UInt(16.W))        // 中间值
  val state = RegInit(0.U(2.W))     // 状态机状态
  val result = RegInit(0.U(16.W))   // 存储最终结果
  mid := 0.U
  // 状态机
  switch(state) {
    is(0.U) { // sIdle
      when(io.start) {
        // 初始化查找区间
        low := 1.U
        high := io.in >> 1 // 初始范围为 [1, in/2]
        state := 1.U // 转到计算状态
      }
    }
    is(1.U) { // sCompute
      mid := (low + high) >> 1 // 计算中间值
      when(mid * mid <= io.in) {
        result := mid // 更新结果
        low := mid + 1.U // 更新下限
      }.otherwise {
        high := mid - 1.U // 更新上限
      }

      // 检查是否完成
      when(low > high) {
        state := 2.U // 转到完成状态
      }
    }
    is(2.U) { // sDone
      state := 0.U // 回到空闲状态
    }
  }

  io.out := result
  io.done := (state === 2.U)
}
