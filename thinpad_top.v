`default_nettype wire

module thinpad_top(
    input wire clk_50M,           
    input wire clk_11M0592,       

    input wire clock_btn,         
    input wire reset_btn,      
	// BaseRam I/O
    inout wire[31:0] base_ram_data,  
    output wire[19:0] base_ram_addr, 
    output wire[3:0] base_ram_be_n, 
    output wire base_ram_ce_n,      
    output wire base_ram_oe_n,      
    output wire base_ram_we_n,       
	// ExtRam I/O
    inout wire[31:0] ext_ram_data,  
    output wire[19:0] ext_ram_addr, 
    output wire[3:0] ext_ram_be_n,  
    output wire ext_ram_ce_n,       
    output wire ext_ram_oe_n,       
    output wire ext_ram_we_n,       
	// Uart I/O 
    output wire txd,  
    input  wire rxd,  
    // DontCare Signals
    input  wire[3:0]  touch_btn,  
    input  wire[31:0] dip_sw,     
    output wire[15:0] leds,       
    output wire[7:0]  dpy0,       
    output wire[7:0]  dpy1,  
    output wire [22:0]flash_a,      
    inout  wire [15:0]flash_d,      
    output wire flash_rp_n,         
    output wire flash_vpen,         
    output wire flash_ce_n,         
    output wire flash_oe_n,         
    output wire flash_we_n,         
    output wire flash_byte_n,       
    output wire[2:0] video_red,    
    output wire[2:0] video_green,  
    output wire[1:0] video_blue,   
    output wire video_hsync,       
    output wire video_vsync,       
    output wire video_clk,         
    output wire video_de        
);

// PLL分频示例
wire locked, clk_10M, clk_20M;
pll_example clock_gen 
 (
  // Clock in ports
  .clk_in1(clk_50M),  
  // Clock out ports
  .clk_out1(clk_10M), 
  .clk_out2(clk_20M), 
  // Status and control signals
  .reset(reset_btn), // PLL复位输入
  .locked(locked)    // PLL锁定指示输出�??"1"表示时钟稳定�??
                     // 后级电路复位信号应当由它生成（见下）
 );

reg reset_of_clk10M;
// 异步复位，同步释放，将locked信号转为后级电路的复位reset_of_clk10M
always@(posedge clk_10M or negedge locked) begin
    if(~locked) reset_of_clk10M <= 1'b1;
    else        reset_of_clk10M <= 1'b0;
end

wire [31:0] baseRam_rdata;
wire [31:0] baseRam_wdata;
assign baseRam_rdata = base_ram_data;
assign base_ram_data = !base_ram_we_n ? baseRam_wdata : 32'hZZZZZZZZ;

wire [31:0] extRam_rdata;
wire [31:0] extRam_wdata;
assign extRam_rdata = ext_ram_data;
assign ext_ram_data = !ext_ram_we_n ? extRam_wdata : 32'hZZZZZZZZ;

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

wire[7:0] number;
SEG7_LUT segL(.oSEG1(dpy0), .iDIG(number[3:0]));
SEG7_LUT segH(.oSEG1(dpy1), .iDIG(number[7:4]));

reg[15:0] led_bits;
assign leds = led_bits;

always@(posedge clock_btn or posedge reset_btn) begin
    if(reset_btn)begin 
        led_bits <= 16'h1;
    end
    else begin 
        led_bits <= {led_bits[14:0],led_bits[15]};
    end
end

wire [11:0] hdata;
assign video_red = hdata < 266 ? 3'b111 : 0; 
assign video_green = hdata < 532 && hdata >= 266 ? 3'b111 : 0; 
assign video_blue = hdata >= 532 ? 2'b11 : 0; 
assign video_clk = clk_50M;
vga #(12, 800, 856, 976, 1040, 600, 637, 643, 666, 1, 1) vga800x600at75 (
    .clk(clk_50M), 
    .hdata(hdata), 
    .vdata(),     
    .hsync(video_hsync),
    .vsync(video_vsync),
    .data_enable(video_de)
);

endmodule
