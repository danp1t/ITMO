    .data

input_addr:      .word  0x80
output_addr:     .word  0x84
const_32: .word 32
count_zero: .word 0
const_1: .word 1

    .text
_start:
    @p input_addr a! @
    @p const_32 a! 
    @p count_zero !b

    loop

    @b
    @p output_addr a! !
    
    halt

loop:
    dup
    @p const_1 
    and
    if inc_count_zero
    shift_right ;

inc_count_zero:
    lit 1
    @b
    +
    !b
    shift_right ;

shift_right:
    2/
    lit -1
    a
    +
    a!
    a
    lit -1
    +
    -if loop
    ;


