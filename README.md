# NSCSCC2024
本文档对具体代码结构做出解释，参赛经验总结见[龙芯杯参赛经验总结](./龙芯杯竞赛总结-马奕斌.pdf)
## 仓库结构概述
* 工程仓库 `chisel-proj` 为 `fork` [chisel-template](https://github.com/chipsalliance/chisel-template) 并添加了若干目录而来。
    * `/func/`：汇编代码及测试数据文件目录
    * `/project`: 可忽略
    * `/src`: 源码目录
    * `/toolkit`: 部分脚本工具，可忽略 
* 终端工具 `term` 为大赛提供的远程连接串口工具，使用方式见该目录下[README](./term/README.md)
* `thinpad_top.v`: vivado工程top模块，用于综合实现。
### chisel-proj/func/
该目录为测试相关初始化文件，具体用法可查看[README](./chisel-proj/func/README.md)
* `asm`: 测试汇编代码， `Makefile` 会编译该目录中的 `foo.S`并生成十六进制指令码`.txt`文件，你可以修改 `Makefile` 使其编译该目录中的所有汇编文件。
* `bintests`: 测试指令文件
* `labs`: 平台测试文件
* `snippets`: 汇编练习文件。由于后面发现可以直接`Chisel`写硬件加速，写一半就没写了
* `data.txt`: `Extram`数据文件

### chisel-proj/src/test/scala/
该目录下存放测试文件。
* `DivSpec.scala`: 单元测试示例，除法模块测试
* `SocSpec.scala`: 集成测试示例，Soc测试

如果你只想看波形，可参考 `SocSpec.scala` 的代码：
```scala
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class SocSpec extends AnyFlatSpec with ChiselScalatestTester {
  "Soc" should "Pass" in {
    test (new SoC()) { core =>
      core.clock.setTimeout(0) 
      step(500) // 500 cycles
    }
  }
}
```
然后执行：
```bash
# cd to chisel-proj/
sbt "testOnly SocSpec -- -DwriteVcd=1"
cd test_run_dir/Soc_should_pass/
# make sure you install gtkwave
gtkwave SoC.vcd
```

### chisel-proj/src/main/scala/
#### SoC.scala
集成模块，用于集成测试。你可以修改sram的初始化文件。
```scala
val cpu = Module(new Cpu)   // change to your cpu
val baseRam = Module(new BaseSram("./func/bintests/foo.txt")) // baseram init
val extRam = Module(new ExtSram("./func/data.txt")) // extram init
```
#### ultra/
* `Cpu.scala`: 测试用CPU，通过单周期读写式寄存器堆模拟`Block Memory`。
* `UltraCpu.scala`: 使用 `BlackBox`实现`Block Memory`，在vivado工程中实例化IP核。
* `helper/`
    * `MultiMux1`: 从 [invalid-cpu](https://github.com/Invalid-Syntax-NSCSCC/invalid-cpu) 中借用的模块，多个模块输出中选择一个，用于译码模块的实现。
    * 其他模块为硬件加速相关，可忽略。
##### bus/
* `sram/`: `Baseram`，`Extram` 模拟
* `uart/`: `BlackBox`实现，仅用于集成
* `GammaBus.scala`: 最终采用的总线设计
##### pipeline/
* `Pipeline.scala`: 集成到 `Cpu` 的模块，连接了流水线各阶段和Plugin
* `UltraPipeline.scala`: 集成到 `UltraCpu` 的模块，同上。
* `UltraFetchPlugin.scala`: 集成到 `UltraPipeline` 的模块，使用 `BlackBox` 实现 `Block Memory`
* `regfile/`: 寄存器堆相关实现
* `fetch/`: 取指模块相关实现
* `decode/`: 译码模块相关实现
* `exe/`: 执行模块相关实现
    * `GammaExeStage.scala`: 仅包含性能测试所要求的指令
    * `UltraExeStage.scala`: 集成了除法模块
    * `DivU.scala`: 除法模块，移植自 `invalid-cpu`
    * `Alu.scala`: 常规算术运算等
    * `Clz.scala`: 除法模块相关，计算前导零，移植自`invalid-cpu` 
##### caches/
* `blkmem/`: `Block Memory` 模拟器
* `icache/`: `icache`相关实现
* `FetchPlugin.scala`: 集成到 `Pipeline` 的模块，Icache + BTFNT分支预测

## 代码概述
### thinpad_top.v
vivado 工程的 top 模块，仅需实例化核以及连接外设，核心代码如下：
```verilog
// divide sram.data into rdata and wdata
wire [31:0] baseRam_rdata;
wire [31:0] baseRam_wdata;
assign baseRam_rdata = base_ram_data;
assign base_ram_data = !base_ram_we_n ? baseRam_wdata : 32'hZZZZZZZZ;

wire [31:0] extRam_rdata;
wire [31:0] extRam_wdata;
assign extRam_rdata = ext_ram_data;
assign ext_ram_data = !ext_ram_we_n ? extRam_wdata : 32'hZZZZZZZZ;

// Instantiate UltraCpu
UltraCpu core(
	.clock(clk_10M),
	.reset(reset_of_clk10M),
	
	.io_baseSram_rspns_rData(baseRam_rdata),
    .io_baseSram_req_wData(baseRam_wdata),
    .io_baseSram_req_addr(base_ram_addr),
    .io_baseSram_req_byteSelN(base_ram_be_n),
    .io_baseSram_req_ce(base_ram_ce_n),
    .io_baseSram_req_oe(base_ram_oe_n),
    .io_baseSram_req_we(base_ram_we_n),

    .io_extSram_rspns_rData(extRam_rdata),
    .io_extSram_req_wData(extRam_wdata),
    .io_extSram_req_addr(ext_ram_addr),
    .io_extSram_req_byteSelN(ext_ram_be_n),
    .io_extSram_req_ce(ext_ram_ce_n),
    .io_extSram_req_oe(ext_ram_oe_n),
    .io_extSram_req_we(ext_ram_we_n),

    .io_uart_rxd(rxd),
    .io_uart_txd(txd)
);
```