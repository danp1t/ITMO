.data
input_addr:      .word  0x80        ; Адрес входного значения
output_addr:     .word  0x84        ; Адрес результата
init_sp:         .word  0x1000      ; Начальное значение стека

.org 0x100
.text
_start:
    lui      sp, %hi(init_sp)
    addi     sp, sp, %lo(init_sp)
    lw       sp, 0(sp)

    addi     sp, sp, -4
    sw       ra, 0(sp)
    jal      ra, main
    lw       ra, 0(sp)
    addi     sp, sp, 4
    halt

main:
    addi     sp, sp, -12
    sw       ra, 0(sp)
    sw       s0, 4(sp)
    sw       s1, 8(sp)

    jal      ra, load_input
    mv       s0, a0

    jal      ra, validate_input
    beqz     a0, handle_error

    mv       a0, s0
    jal     ra, compute_sum
    mv       s1, a0
    j        save_result

handle_error:
    addi     s1, zero, -1          

save_result:
    ; Сохранение результата
    lui      t0, %hi(output_addr)
    addi     t0, t0, %lo(output_addr)
    lw       t0, 0(t0)
    sw       s1, 0(t0)

    lw       ra, 0(sp)
    lw       s0, 4(sp)
    lw       s1, 8(sp)
    addi     sp, sp, 12
    jr       ra

load_input:
    lui      t0, %hi(input_addr)
    addi     t0, t0, %lo(input_addr)
    lw       t0, 0(t0)
    lw       a0, 0(t0)
    jr       ra

validate_input:
    addi    t0, zero, 0    
    bgt     a0, t0, valid    
    addi    a0, zero, 0   
    j       end_validate
valid:
    addi    a0, zero, 1   
end_validate:
    jr      ra       

compute_sum:
    addi     sp, sp, -8
    sw       ra, 0(sp)
    sw       s2, 4(sp)

    addi     t0, a0, 1
    mul      t1, a0, t0
    mulh     t2, a0, t0

    bgt     t2,  zero, overflow
    addi     t4, t4, 1
    sra     a0, t1, t4            
    j        end_compute

overflow:
    lui      a0, 0xCCCCC       
    addi     a0, a0, 0xCCC

end_compute:
    lw       ra, 0(sp)
    lw       s2, 4(sp)
    addi     sp, sp, 8
    jr       ra