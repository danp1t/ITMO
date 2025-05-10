    .data

input_addr:      .word  0x80
output_addr:     .word  0x84
byte_const_1:    .byte  1
buffer:          .word  0x40
const_65:        .word  65
const_122:       .word  122
const_10:        .word  10
const_32:        .word  32
flag:            .word  1
const_90:        .word  90
const_97:        .word  97
const_1:         .word  1
buffer_ptr:      .word  0x0 
const_5F: .word 0x5F


.org             0x250
    .text

_start:
loop:
    load_ind     input_addr
    store_ind    buffer

    ; Проверка на перенос строки
    load_ind     buffer
    sub          const_10
    beqz         exit

    ; Проверка на пробел
    load_ind     buffer
    sub          const_32
    beqz         change_flag
    load_ind     buffer
    sub          const_65
    ble          not_letter

    load_ind     buffer
    sub          const_90
    bgt          is_little

continue_loop_upper:
    load         flag
    sub          const_1
    bnez        make_little_letter
    load_ind buffer
    store_ind    output_addr
    store_ind    buffer_ptr  
    load_imm     0
    store        flag 
    load         buffer_ptr
    add          const_1
    store        buffer_ptr
    jmp          loop

make_little_letter:
    load_ind     buffer
    add          const_32
    store_ind    output_addr
    store_ind    buffer_ptr   
    load         buffer_ptr
    add          const_1
    store        buffer_ptr
    jmp          loop

continue_loop_little:
    load_ind     buffer
    sub          const_122
    bgt          not_letter
    load         flag
    sub          const_1
    bnez       save_little_symbol
    load_ind     buffer
    sub          const_32
    store_ind    output_addr
    store_ind    buffer_ptr   
    load_imm     0
    store        flag
    load         buffer_ptr
    add          const_1
    store        buffer_ptr
    jmp          loop

save_little_symbol:
    load_ind     buffer
    store_ind    output_addr
    store_ind    buffer_ptr
    load         buffer_ptr
    add          const_1
    store        buffer_ptr
    jmp          loop

change_flag:
    load_imm     1
    store        flag
    load         const_32
    store_ind    output_addr
    store_ind    buffer_ptr
    load         buffer_ptr
    add          const_1
    store        buffer_ptr
    jmp          loop  

is_little:
    load_ind     buffer
    sub          const_97
    ble          not_letter
    jmp          continue_loop_little

not_letter:
    load_ind buffer
    store_ind    output_addr
    store_ind    buffer_ptr
    load         buffer_ptr
    add          const_1
    store        buffer_ptr
    jmp          loop

exit:
    load_imm     0
    store_ind    buffer_ptr
    load         buffer_ptr
    add          const_1
    store        buffer_ptr

fill_loop:
    load         buffer_ptr
    sub          const_32     
    bgt          end_fill       

    load         const_5F
    store_ind    buffer_ptr
    load         buffer_ptr
    add          const_1
    store        buffer_ptr
    jmp          fill_loop

end_fill:
    halt