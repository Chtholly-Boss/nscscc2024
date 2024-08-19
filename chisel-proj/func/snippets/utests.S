# This snippet contains the user tests 
# * fib: calculate n fib
# * stream: continious Load/Store
# * matrix: matrix multiplication
# * Cryptonight
    .org 0x0
    .text
    .global _start
_start:
    nop
Fib:
    addi.w      $t0,$zero,0x1   # t0 = 1
    addi.w      $t1,$zero,0x1   # t1 = 1
    lu12i.w     $a0,-0x7fc00    # a0 = 0x80400000
    addi.w      $a1,$a0,0x100   # a1 = 0x80400100
    FibLoop:
        add.w       $t2,$t0,$t1     # t2 = t0+t1
        addi.w      $t0,$t1,0x0     # t0 = t1
        addi.w      $t1,$t2,0x0     # t1 = t2
        st.w        $t2,$a0,0x0
        ld.w        $t3,$a0,0x0
        bne         $t2,$t3,end
        addi.w      $a0,$a0,0x4     # a0 += 4
        bne         $a0,$a1,FibLoop
FibEnd:
    jirl    $r0,$ra,0x0

Stream:
    li.w        $a0,0x80100000
    li.w        $a1,0x80400000
    li.w        $a2,0x00300000
    add.w       $a2,$a0,$a2
    Stream_Next:
        ld.w        $t0,$a0,0x0
        st.w        $t0,$a1,0x0
        addi.w      $a0,$a0,0x4
        addi.w      $a1,$a1,0x4
        bne         $a0,$a2,Stream_Next
    Stream_End:
        jirl        $r0,$ra,0x0

MatrixMul:
    li.w        $a0, 0x80400000
    li.w        $a1, 0x80410000
    li.w        $a2, 0x80420000
    li.w        $a3, 4
    or          $t8,$r0,$r0
    loop1:
        beq         $t8,$a3,loop1end

        slli.w      $t0,$t8,2
        slli.w      $t2,$t8,9
        add.w       $t0,$a0,$t0
        add.w       $t2,$a1,$t2
        or          $t1,$r0,$r0
        loop2:
            beq         $t1,$a3,loop2end

            ld.w        $t7,$t0,0x0
            slli.w      $a4,$t1,9
            add.w       $a4,$a2,$a4
            or          $t4,$t2,$r0
            or          $t3,$r0,$r0
            loop3:
                beq         $t3,$a3,loop3end

                addi.w      $t3,$t3,1
                ld.w        $t5,$t4,0x0
                ld.w        $t6,$a4,0x0
                mul.w       $t5,$t7,$t5
                addi.w      $a4,$a4,4
                addi.w      $t4,$t4,4
                add.w       $t5,$t6,$t5
                st.w        $t5,$a4,-4
                b           loop3

            loop3end:
                addi.w      $t1,$t1,1
                addi.w      $t0,$t0,512
                b           loop2
        loop2end:
            addi.w      $t8,$t8,1
            b           loop1
    loop1end:
        jirl        $r0,$ra,0x0

UTEST_CRYPTONIGHT:
    // a0 -> pad
    // a1 -> a
    // a2 -> b
    // a3 -> n
    li.w        a0, 0x80400000
    li.w        a1, 0xdeadbeef
    li.w        a2, 0xfaceb00c
    li.w        a3, 0x100000
    or          t4,zero,a0
    or          t3,zero,zero
    li.w        t0,0x80000
    fill_next:
        st.w        t3,t4,0
        addi.w      t3,t3,1
        addi.w      t4,t4,4
        bne         t3,t0,fill_next

        or          t1,zero,zero
        li.w        t2,0x7ffff
    crn_hext:
        and         t0,a1,t2
        slli.w      t0,t0,2
        add.w       t0,a0,t0
        ld.w        t3,t0,0
        srli.w      t4,a1,1
        slli.w      t3,t3,1
        xor         t3,t3,t4
        and         t4,t3,t2
        xor         a2,t3,a2
        slli.w      t4,t4,2
        st.w        a2,t0,0
        add.w       t4,a0,t4
        ld.w        t0,t4,0
        or          a2,zero,t3
        mul.w       t3,t3,t0
        addi.w      t1,t1,1
        add.w       a1,t3,a1
        st.w        a1,t4,0
        xor         a1,t0,a1
        bne         a3,t1,crn_hext
crn_end:
    jirl        zero,ra,0x0
    /* End of test */
