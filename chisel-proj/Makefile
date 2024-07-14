# LoongArch GNU Tool
ifndef CROSS_COMPILE
CROSS_COMPILE=loongarch32r-linux-gnusf-
endif
AS=$(CROSS_COMPILE)as
LD=$(CROSS_COMPILE)ld
OBJCOPY=$(CROSS_COMPILE)objcopy
OBJDUMP=$(CROSS_COMPILE)objdump 
# Directory Defining
TOOLKITS=./toolkit
FUNC=./func
TEST_HOME=$(FUNC)/bintests/
ASM_HOME=$(FUNC)/asm
VIVADO=../vivado-proj
VERILOG_SRC=$(VIVADO)/thinpad_top.srcs/sources_1/new

# Tasks
# - Add Your Test Target Here
TEST_OBJECTS = fib.txt matrix.txt stream.txt
test: $(TEST_OBJECTS)
	cp *.txt $(TEST_HOME)
	rm -f *.o *.om *.bin *.data *.txt
%.o:$(ASM_HOME)/%.S
	@echo "Assembling..."
	$(AS) $< -o $@
%.om:%.o $(FUNC)/ram.ld  
	@echo "Linking..."
	@$(LD) -T $(FUNC)/ram.ld $< -o $@
%.bin:%.om 
	@echo "Converting..."
	$(OBJCOPY) -O binary $< $@
%.data:%.bin
	@$(TOOLKITS)/Bin2Mem.exe -f $< -o $@
%.txt:%.data
	@$(TOOLKITS)/Converter.sh < $< > $@


verilog:
	@echo "Generating Verilog File..."
	@sbt run > /dev/null
	@echo "Moving Verilog Src to Vivado project..."
	@mv *.sv $(VERILOG_SRC)
	@echo "Changing to the Src Directory"
	@cd $(VERILOG_SRC)
clean:
	rm -rf $(TEST_HOME)/*
