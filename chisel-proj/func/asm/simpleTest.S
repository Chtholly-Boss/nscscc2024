    .org 0x0
    .text
    .global _start
_start:
    ori     $r1,$r0,0x2
    ori     $r2,$r0,0x3
    nop
    mul.w     $r3,$r1,$r2
