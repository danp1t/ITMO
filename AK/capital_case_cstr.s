    .data


input_addr:      .word  0x80
output_addr:     .word  0x84
byte_const_1: .byte 1
buffer: .word 0x01
const_65: .word 65
const_122: .word 122
const_10: .word 10
const_32: .word 32
flag:       .word 1
const_90: .word 90
const_97: .word 97
const_1: .word 1


.org 0x250
    .text

_start:
    ; Над написать цикл, чтобы он читал буквы пока не найдет \n перенос строки
    ; Чтобы сделать букву заглавной надо вычесть из неё 32
    ; Надо чтобы он сделал минус первой букве и каждой букве после пробела
    ; Код пробела 0x20 или 32
    ; Код переноса строки 0x0A или 10 [Done]
    ; Сделать проверку, что у нас буква, иначе возратить -1 [Done]
    ; Буквы от 0x41 - 0x7A или 65 - 122 [Done] - тут проблема в том, что там дырка есть в диапазоне... с 65 до 90 и с 97 по 122

loop:
    ; Загружаем число в аккумулятор, нам надо увеличить input_addr аккамулятор, если он не равен переносу строки
    load_ind     input_addr  
    store_ind    buffer

     ; Проверка на перенос строки
    load_ind buffer
    sub const_10
    beqz exit

     ; Проверка на пробел 
    load_ind buffer
    sub const_32
    beqz change_flag

    ; Проверка на ввод буквы
    load_ind buffer
    sub const_65
    ble not_in_domain

    load_ind buffer
    sub const_90 
    bgt is_upper

continue_loop:
    load_ind buffer
    sub const_122
    bgt not_in_domain

    ; Проверка установлен ли флаг
    load flag
    sub const_1
    beqz make_great_symbol

    load_ind buffer
    store_ind    output_addr
    
    jmp loop

is_upper:
    load_ind buffer
    sub const_97
    ble not_in_domain
    jmp continue_loop

make_great_symbol:
    load_imm 0
    store flag
    load_ind buffer
    sub const_32
    store_ind output_addr
    jmp loop

change_flag:
    load_imm 1
    store flag
    load const_32
    store_ind output_addr
    jmp loop

exit:
    halt

not_in_domain:
    load_imm    -1
    store_ind    output_addr
    halt