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

REPO_1=/home/cauchy2b/repos_git/plat1/2024673
REPO_2=/home/cauchy2b/repos_git/plat2/2024673
REPO_3=/home/cauchy2b/repos_git/plat3/2024673
# Tasks
# - Add Your Test Target Here
TEST_OBJECTS = foo.txt
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

repo1:
	cp *.sv $(REPO_1)/thinpad_top.srcs/sources_1/new/
	cp *.S 	$(REPO_1)/asm
	@echo "entering repo of platform1..."
	cd $(REPO_1)
repo2:
	cp *.sv $(REPO_2)/thinpad_top.srcs/sources_1/new/
	cp *.S 	$(REPO_2)/asm
	@echo "entering repo of platform2..."
	cd $(REPO_2)
repo3:
	cp *.sv $(REPO_3)/thinpad_top.srcs/sources_1/new/
	cp *.S 	$(REPO_3)/asm
	@echo "entering repo of platform3..."
	cd $(REPO_3)
clean:
	rm -rf $(TEST_HOME)/*