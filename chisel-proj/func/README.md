# ASM Guide
## Setup
For Seniorious, you can use the following instructions only.

* addi.w, add.w, sub.w, mul.w
* lu12i.w pcaddu12i, sltui.w
* or, ori, andi, and, xor
* srli.w, slli.w, srl.w
* jirl 
* b, beq, bne, bl, bge
* st.w, ld.w, st.b, ld.b

add your asm in ./func/asm and add it to Makefile. Then run
```bash
# This will compile the asm
# in the proj root
make test
# You'd better add a nop at the beginning
# There exists a bug in the test.
# the instruction at 0x0 wouldn't be execute
```
Then modify `SoC.scala` to load the program in Baseram. After that, run:
```bash
# in the proj root
sbt "testOnly SocSpec -- -DwriteVcd=1"
cd test_run_dir/Soc_should_pass
gtkwave SoC.vcd
```
Then you can Check the results.

## Conventions
You'd better not specify registers randomly.

| alias | logic register | use |
| :---: | :---: | :---: |
| zero | r0 | number 0 |
| ra | r1 | return addr |
| a0-a7 | r4-r11 | parameters |
| t0-t8 | r12-r20 | local variables |
| s0-s8 | r23-r31 | local variables |

## More Instructions
You'd better add the following instructions into the core:

* blt
* sll
