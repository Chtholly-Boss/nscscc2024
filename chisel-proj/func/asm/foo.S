    .org 0x0
    .text
    .global _start
_start:
    nop
    li.w    $a1,36
    li.w    $a2,2
    div.w   $a0,$a1,$a2
    div.w   $a0,$a0,$a2
    mul.w   $a0,$a0,$a1
    div.w   $a0,$a0,$a2
    div.w   $a0,$a0,$a2
    div.w   $a0,$a0,$a2
_end:
    b   _end
