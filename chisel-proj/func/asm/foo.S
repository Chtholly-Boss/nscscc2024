    .org 0x0
    .text
    .global _start
_start:
    nop
    li.w    $a1,20000
    bl  Sqrt
_end:
    b   _end
Sqrt:
    li.w    $t1,1
    or      $t2,$r0,$a1
    SqrtLoop:
        bge     $t1,$t2,SqrtLoopEnd
        sub.w   $s0,$t2,$t1
        srli.w  $s0,$s0,1
        add.w   $s0,$s0,$t1 
        mul.w   $t0,$s0,$s0
        beq     $t0,$a1,SqrtLoopEnd
        bge     $t0,$a1,SqrtMore
        SqrtLess:
            or  $t1,$r0,$s0     # left = mid
            addi.w  $t1,$t1,1   # left = mid + 1
            b   SqrtLoop
        SqrtMore:
            or  $t2,$r0,$s0     # right = mid
            b       SqrtLoop
    SqrtLoopEnd:
        bge     $a1,$t0,REVISE
        addi.w    $a0,$s0,-1
        b   SqrtEnd
        REVISE:
            addi.w  $a0,$s0,0
SqrtEnd:
    jirl    $r0,$ra,0x0

