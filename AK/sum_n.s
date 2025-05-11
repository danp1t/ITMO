    .data

input_addr:      .word  0x80               ; Input address where the number 'n' is stored
output_addr:     .word  0x84               ; Output address where the result should be stored

    .text

_start:
    lui      t0, %hi(input_addr)             
    addi     t0, t0, %lo(input_addr)         
    lw       t0, 0(t0)                   
    lw       t1, 0(t0)                       

sum_begin:
    addi    t2, zero, 0                  

sum_loop:
    beqz    t1, sum_end                  
    add     t2, t2, t1    
    addi    t1, t1, -1
    j       sum_loop

sum_end:
    lui     t0, %hi(output_addr)
    addi    t0, t0, %lo(output_addr)
    lw      t0, 0(t0) 
    sw      t2, 0(t0)
    halt            