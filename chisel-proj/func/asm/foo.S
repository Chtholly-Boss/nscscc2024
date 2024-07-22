    .org 0x0
    .text
    .global _start
_start:
    addi.w      $r1,$zero,0x2   # t0 = 2
    addi.w      $r2,$zero,0x3   # t1 = 3
    mul.w       $r3,$r1,$r2
    addi.w      $r1,$r1,0x2   # t0 = 4
    addi.w      $r2,$r2,0x3   # t1 = 6
    mul.w       $r3,$r1,$r2
end:
    beq         $zero,$zero,end
