# This snippet contains the array operations
# place arguments at a1-a7 and then `bl func`
# Following Functions are supported:
# * Reverse(array,length): reverse the array in place
# * Print(array,length): print to a0 in order
# * Max(array,length): find the max and store at a0
# * Min(array,length)
# TODO * Sort(array,length)
# TODO * Average(array,length)

    .org 0x0
    .text
    .global _start
_start:
    nop
    # Give the array and its length
    li.w    $a1,0x80400000  # array at extram 0
    li.w    $a2,10          # length

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


Reverse:
    # Inplace Reverse
    # you can modify the dst to another place 
    # just a little more work
    # * for(int i = 0; i < bound; i++){
    # *   swap(arr[i],arr[n-1-i]);
    # * }
    # t0: loop bound
    # t1: end addr of the array
    # t2: offset
    # t3: arr[i]
    # t4: arr[n-1-i]
    srli.w  $t0,$a2,1   # length/2: specify the loop bound
    # calculate the end of the array
    addi.w   $s0,$a2,-1   # length - 1
    slli.w  $s0,$s0,2   # int is 4 bytes
    add.w  $t1,$a1,$s0 # store the addr of the final element
    li.w    $t2,0
    ReverseLoop:
        bge     $t2,$t0,ReverseLoopEnd
        # === Loop body ===
        slli.w   $s7,$t2,2      # int is 4 bytes
        add.w    $s1,$a1,$s7    # &arr[i]
        sub.w    $s2,$t1,$s7    # &arr[n-1-i]
        ld.w     $t3,$s1,0     # arr[i]
        ld.w     $t4,$s2,0     # arr[n-1-i]
        st.w     $t3,$s2,0     # *arr[n-1-i] = arr[i]
        st.w     $t4,$s1,0     # *arr[i] = arr[n-1-i]
        # =================
        addi.w    $t2,$t2,1
        b       ReverseLoop
    ReverseLoopEnd:
        jirl    $r0,$ra,0x0
        
Print:
    # Print elements of array in order to a0
    # check a0 to see the results
    # * for(int i = 0; i < length; i++){
    # *     a0 = arr[i]
    # * }
    # t0: offset
    li.w    $t0,0
    PrintLoop:
        bge     $t0,$a2,PrintLoopEnd
        # === body ===
        slli.w  $s7,$t0,2
        add.w   $s0,$a1,$s7 # &arr[i]
        ld.w    $a0,$s0,0
        # ============
        addi.w  $t0,$t0,1
        b       PrintLoop
    PrintLoopEnd:
        jirl    $r0,$ra,0x0


Max:
    # Find the Maximum element in the array
    # store the result at a0
    # ! Couldn't handle empty
    # ! You should check before calling this function
    # * a0 = arr[0];
    # * for(int i = 1; i < length; i++){
    # *     if( arr[i] > a0){
    # *         a0 = arr[i]
    # *     }
    # * }
    # t0: offset
    # t1: arr[i]
    ld.w    $a0,$a1,0 # a0 = arr[0]
    li.w    $t0,1
    MaxLoop:
        bge     $t0,$a2,MaxLoopEnd
        slli.w  $s7,$t0,2
        add.w   $s1,$a1,$s7
        ld.w    $t1,$s1,0
        bge     $a0,$t1,NextMaxLoop
        or      $a0,$r0,$t1
    NextMaxLoop:
        addi.w  $t0,$t0,1
        b       MaxLoop
    MaxLoopEnd:
        jirl    $r0,$ra,0x0

_end:
    nop