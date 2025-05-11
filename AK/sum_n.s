    .data

input_addr:      .word  0x80               ; Input address where the number 'n' is stored
output_addr:     .word  0x84               ; Output address where the result should be stored

    .text

_start:
    lui      t0, %hi(input_addr)             
    addi     t0, t0, %lo(input_addr)         
    lw       t0, 0(t0)                   
    lw       t1, 0(t0)          

    addi    t3, zero, 0                   
    beqz     t1, negative_input 
    bgt t3, t1, negative_input 

    addi    t2, t1, 1                   
    mul     t3, t1, t2
    mulh   t5, t1, t2
    bgt t5, zero, overflow_error
    addi    t4, t4, 1    
    sra    t3, t3, t4     

save_result:
    lui     t0, %hi(output_addr)         
    addi    t0, t0, %lo(output_addr)
    lw      t0, 0(t0)
    sw      t3, 0(t0)                   
    halt

negative_input:
    addi    t3, t3, -1    
    j save_result

overflow_error:
    lui     t3, 0xCCCCC
    addi    t3, t3, 0xCCC 
    j save_result      

        