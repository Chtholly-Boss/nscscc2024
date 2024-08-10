# This snippet contains the array operations
    .org 0x0
    .text
    .global _start
_start:
    nop
    # Give the array and its length
    li.w    $a1,0x80400000  # array at extram 0
    li.w    $a2,10          # length
    bl      Min
_end:
    jirl    $r0,$ra,0x0
Min:
    # Find the Minimum element in the array
    # store the result at a0
    # ! Couldn't handle empty
    # ! You should check before calling this function
    # * a0 = arr[0];
    # * for(int i = 1; i < length; i++){
    # *     if( arr[i] < a0){
    # *         a0 = arr[i]
    # *     }
    # * }
    # t0: offset
    # t1: arr[i]
    ld.w    $a0,$a1,0 # a0 = arr[0]
    li.w    $t0,1
    MinLoop:
        bge     $t0,$a2,MinLoopEnd
        slli.w  $s7,$t0,2
        add.w   $s1,$a1,$s7
        ld.w    $t1,$s1,0
        bge     $t1,$a0,NextMinLoop
        or      $a0,$r0,$t1
    NextMinLoop:
        addi.w  $t0,$t0,1
        b       MinLoop
    MinLoopEnd:
        jirl    $r0,$ra,0x0


