    .org 0x0
    .text
    .global _start
_start:
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
