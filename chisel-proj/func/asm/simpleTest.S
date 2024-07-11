    .org 0x0
    .text
    .global _start
_start:
    beq         zero,zero,fib

info:
    .asciz "Fib Finish."

    .p2align 2
feed:
    .asciz "All PASS!"

    .p2align 2
fib:
    addi.w      t0,zero,0x1   # t0 = 1
    addi.w      t1,zero,0x1   # t1 = 1
    lu12i.w     a0,-0x7fc00    # a0 = 0x80400000
    addi.w      a1,a0,0x100   # a1 = 0x80400100
loop0:
    add.w       t2,t0,t1     # t2 = t0+t1
    addi.w      t0,t1,0x0     # t0 = t1
    addi.w      t1,t2,0x0     # t1 = t2
    st.w        t2,a0,0x0
    ld.w        t3,a0,0x0
    bne         t2,t3,end
    addi.w      a0,a0,0x4     # a0 += 4
    bne         a0,a1,loop0

WRITESERIAL:
    lu12i.w     s0,-0x40300    # s0 = 0xbfd00000
    la.local    s1,info
    ld.b        a0,s1,0x0
loop1:
    addi.w      s1,s1,0x1
.TESTW:
    ld.b        t0,s0,0x3fc
    andi        t0,t0,0x001
    beq         t0,zero,.TESTW
    st.b        a0,s0,0x3F8
    ld.b        a0,s1,0x0
    bne         a0,zero,loop1
