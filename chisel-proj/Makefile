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
VCD_REPO=/home/cauchy2b/repos_git/nscscc2024/chisel-proj/test_run_dir
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

single:$(VCD_REPO)/Single_should_Pass/*
	sbt "testOnly SingleSpec -- -DwriteVcd=1"
double:$(VCD_REPO)/Double_should_Pass/*
	sbt "testOnly DoubleSpec -- -DwriteVcd=1"
repo1:
	cp *.s 	$(REPO_1)/asm
repo2:
	cp *.s 	$(REPO_2)/asm
repo3:
	cp *.s 	$(REPO_3)/asm
clean:
	rm -rf $(TEST_HOME)/*
