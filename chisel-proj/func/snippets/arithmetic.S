# This snippet contains the Arithmetic Operations
# place arguments at a1-a7 and then `bl func`
# Following Functions are supported:
# TODO * Division /
# TODO * Modular %
# * Power(int base,int power)
# * Sqrt(int num)
# * Count1(int num)

    .org 0x0
    .text
    .global _start
_start:
    nop
Division:
    # * a0 = a1 / a2, 
    # * a0 = 0
    # * for i in range(31,-1,-1)
    # *     tmp = a2 * (1 << i)
    # *     if  tmp <= a1:
    # *         a0 += tmp
    # *         a1 -= tmp
    # s0: i
    # t0: tmp
    li.w    $a0,0
    li.w    $s0,31
    li.w    $s1,1
    DivLoop:
        # ! sll.w not implement
        sll.w  $t0,$s1,$s0    # tmp = 1 << i 
        # ! blt will be better
        bge $a1,$t0,DivUpdate
        b   DivLoopNext
        DivUpdate:
            add.w   $a0,$a0,$t0 # a0 += tmp
            sub.w   $a1,$a1,$t0 # a1 -= tmp
        DivLoopNext:
        # ! a1 < a2 is enough
            addi.w  $s0,$s0,-1
            bge     $s0,$r0,DivLoop
DivisionEnd:
    jirl    $r0,$ra,0x0

Modular:
    # * a0 = a1 % a2
    # TODO: do things like division but not update quotient
    # TODO: get the a1 when a1 < a2
ModularEnd:
    jirl    $r0,$ra,0x0

Power:
    # * a0 = a1 ** a2
    # Binary Power From OI wiki
    # * a0 = 1
    # * while( a2 > 0 ):
    # *     if a2 & 1:
    # *         a0 = a0 * a1
    # *     a1 = a1 * a1
    # *     a2 = a2 >> 1
    li.w    $a0,1
    PowerLoop:
        beq     $a2,$r0,PowerEnd
        andi    $s0,$a2,1
        beq     $s0,$r0,PowerUpdate
        mul.w   $a0,$a0,$a1
        PowerUpdate:
            mul.w   $a1,$a1,$a1
            srli.w  $a2,$a2,1
        b       PowerLoop
PowerEnd:
    jirl    $r0,$ra,0x0

Sqrt:
    # * a0 = sqrt(a1)
    # binary search, round down
    # * a0 = 1
    # * left, right = 1, a1
    # * while left < right:
    # *     mid = left + (right - left) / 2
    # *     tmp = mid * mid
    # *     if tmp == a1:
    # *         break
    # *     if tmp < a1:
    # *         left = mid
    # *     else:
    # *         right = mid
    # * a0 = mid
    # t1: left
    # t2: right
    # s0: mid
    # t0: tmp
    li.w    $t1,1
    or      $t2,$r0,$a1
    SqrtLoop:
        bge     $t1,$t2,SqrtLoopEnd
        sub.w   $s0,$t2,$t1     # right - left
        srli.w  $s0,$s0,1       # (right - left) / 2
        add.w   $s0,$s0,$t1     # mid = left + (right - left) / 2
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
        addi.w  $a0,$s0,-1
        b   SqrtEnd
        REVISE:
            addi.w  $a0,$s0,0
SqrtEnd:
    jirl    $r0,$ra,0x0

Count1:
    li.w    $a1,0x80e0f001
    # * a0 = the number of 1 in a1
    # * for(;;){
    # *     a1 = a1 >> 1;
    # *     if a0 == 0
    # *        break
    # *     a1++;
    # * }
    # You can gain Count0 by 32-a0
    li.w    $a0,0
    Count1Loop:
        beq     $a1,$r0,Count1LoopEnd
        andi    $t0,$a1,1
        add.w   $a0,$a0,$t0
        srli.w  $a1,$a1,1
        b       Count1Loop
Count1End:
    jirl    $r0,$ra,0x0
