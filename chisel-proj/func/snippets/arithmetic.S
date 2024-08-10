# This snippet contains the Arithmetic Operations
# place arguments at a1-a7 and then `bl func`
# Following Functions are supported:
# TODO * Division /
# TODO * Modular %
# TODO * Power
# TODO * Square
# TODO * Count1
# TODO * BitReverse

    .org 0x0
    .text
    .global _start
_start:
    nop
    # Give the array and its length
    li.w    $a1,0x80400000  # array at extram 0
    li.w    $a2,10          # length
