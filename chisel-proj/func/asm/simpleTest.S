    .org 0x0
    .text
    .global _start
_start:
    ori     $r1,$r0,0x2
    ori     $r2,$r0,0x3
    ld.w    $r3,$r1,0x2
    st.w    $r2,$r1,0x1
    nop
