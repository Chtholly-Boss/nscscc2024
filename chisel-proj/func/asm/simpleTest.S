    .org 0x0
    .text
    .global _start
_start:
    ori     $r1,$r0,0x2
    ori     $r2,$r0,0x3
    nop
    bne     $r1,$r2,_ENTRY_2
_ENTRY_1:
    bge     $r1,$r2,_ENTRY_2
_ENTRY_2:
    b       _ENTRY_1
