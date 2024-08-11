    .org 0x0
    .text
    .global _start
_start:
    nop
    li.w    $a1,3
    li.w    $a2,5
    bl  Power
_end:
    b   _end
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
        mul.w  $t0,$a2,$t0
        # ! blt will be better
        blt $a1,$t0,DivLoopNext
        add.w   $a0,$a0,$t0 # a0 += tmp
        sub.w   $a1,$a1,$t0 # a1 -= tmp
        DivLoopNext:
        # ! a1 < a2 is enough
            addi.w  $s0,$s0,-1
            bge     $s0,$r0,DivLoop
DivisionEnd:
    jirl    $r0,$ra,0x0
