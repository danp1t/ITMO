global _start

section .text
; Принимает код возврата и завершает текущий процесс
exit: 
    mov rax, 60
    mov rdi, 0
    syscall
    ret 

; Принимает указатель на нуль-терминированную строку, возвращает её длину
string_length:
    mov rax, 0
    .loop:
        cmp byte[rax + rdi], 0
        jz .end
    .counter: 
        inc rax
        jmp .loop
    .end:
        ret

; Принимает указатель на нуль-терминированную строку, выводит её в stdout
print_string:
    push rdi
    call string_length
    mov rdx, rax
    mov rax, 1
    pop rsi
    mov rdi, 1
    syscall
    ret

; Принимает код символа и выводит его в stdout
print_char:
    push rdi
    mov rax, 1
    mov rsi, rsp
    pop r9
    mov r9, 0
    mov rdi, 1
    mov rdx, 1
    syscall
    ret
; Переводит строку (выводит символ с кодом 0xA)
print_newline:
    mov rax, 1
    mov rsi, 0xA
    mov rdi, 1
    mov rdx, 1
    syscall
    ret

; Выводит беззнаковое 8-байтовое число в десятичном формате 
; Совет: выделите место в стеке и храните там результаты деления
; Не забудьте перевести цифры в их ASCII коды.
print_uint:
    mov r10, 10 ; Делитель
    sub rsp, 24 ; Освобождаем стек для 20 символов
    mov rax, rdi
    mov rcx, 24 ; Счетчик
    dec rcx
    mov byte[rcx + rsp], 0 ; Создаем конец строки для нуль-детерминированной строки, чтобы далее воспользоваться функцией print_string

    .loop:
        xor rdx, rdx
        dec rcx
        div r10
        add rdx, 48 ; Переводим число в ASCII символ
        mov byte[rcx + rsp], dl ; Сохраняем byte регистра rdx
        cmp rax, 0
        jz .end
        jmp .loop
    .end:
        lea rdi, [rcx + rsp]  ; Адрес начала строки
        call print_string
        add rsp, 24
        ret
    

; Выводит знаковое 8-байтовое число в десятичном формате 
print_int:  
    ; Узнать, положительное число или нет
    cmp rdi, 0
    jl .negative
    jge .positive
    

    .positive:
        sub rsp, 8
        call print_uint
        add rsp, 8
        ret
    .negative:
        push rdi
        mov rdi, '-'
        call print_char
        pop rdi
        neg rdi
        sub rsp, 8
        call print_uint
        add rsp, 8
        ret

; Принимает два указателя на нуль-терминированные строки, возвращает 1 если они равны, 0 иначе
string_equals:
    xor rcx, rcx
    .loop:
        mov al, byte[rsi + rcx]
        cmp byte[rdi + rcx], al
        jne .false
        inc rcx
        cmp al, 0
        jz .end
        jmp .loop
    .false:
        mov rax, 0
        ret
    .end:
        mov rax, 1
        ret

; Читает один символ из stdin и возвращает его. Возвращает 0 если достигнут конец потока
read_char:
    mov rax, 0
    push ax
    mov rdi, 0
    mov rsi, rsp
    mov rdx, 1
    syscall
    pop ax
    ret 

; Принимает: адрес начала буфера, размер буфера
; Читает в буфер слово из stdin, пропуская пробельные символы в начале, .
; Пробельные символы это пробел 0x20, табуляция 0x9 и перевод строки 0xA.
; Останавливается и возвращает 0 если слово слишком большое для буфера
; При успехе возвращает адрес буфера в rax, длину слова в rdx.
; При неудаче возвращает 0 в rax
; Эта функция должна дописывать к слову нуль-терминатор

read_word:
    push r11
    push r12
    mov rcx, rsi ; Размер буфера
    mov r11, rdi ; Адрес буфера
    mov rax, 0
    mov rdi, 0
    xor r12, r12 ; Счетчик символов

    .loop:
        push rcx
        push r11
        push rdx
        call read_char
        pop rdx
        pop r11
        pop rcx
        cmp al, 0x20
        jz .loop
        cmp al, 0x9
        jz .loop
        cmp al, 0xA
        jz .loop
        jmp .loop3


    .loop2:
        push rcx
        push r11
        push rdx
        call read_char
        pop rdx
        pop r11
        pop rcx
        cmp al, 0x20
        jz .end
        cmp al, 0x9
        jz .end
        cmp al, 0xA
        jz .end
        jmp .loop3

    .loop3:
        test al,al   
        jz .end
        cmp rcx, 0
        jz .error
        mov byte[r11 + r12], al
        dec rcx
        inc r12
        jmp .loop2

    .error:
        mov rax, 0
        pop r12
        pop r11
        ret

    .end:
        mov byte [r11 + r12], 0
        mov rax, r11
        mov rdx, r12
        pop r12
        pop r11
        ret
    
 

; Принимает указатель на строку, пытается
; прочитать из её начала беззнаковое число.
; Возвращает в rax: число, rdx : его длину в символах
; rdx = 0 если число прочитать не удалось
parse_uint:
    ; В rdi указатель на строку
    xor rcx, rcx
    xor rax, rax
    mov rsi, 10 ; Множитель

    .loop:
    mov r11b, byte[rdi + rcx]
    ; Проверка, что это число
    cmp r11b, 0
    jz .end
    cmp r11b, 0x30
    jb .end
    cmp r11b, 0x39
    ja .end
    inc rcx
    sub r11b, 0x30 ; Перевод в цифру
    mul rsi
    add rax, r11
    jmp .loop

    .end:
        cmp rcx, 0
        jz .error
        mov rdx, rcx
        ret

    .error:
        mov rdx, 0
        ret


; Принимает указатель на строку, пытается
; прочитать из её начала знаковое число.
; Если есть знак, пробелы между ним и числом не разрешены.
; Возвращает в rax: число, rdx : его длину в символах (включая знак, если он был) 
; rdx = 0 если число прочитать не удалось
parse_int:
    xor rcx, rcx
    mov r11b, byte[rdi + rcx]
    

    cmp r11b, 0x2B
    jz .sign
    cmp r11b, 0x2D
    jz .sign
    sub rsp, 8
    call parse_uint
    add rsp, 8
    
    ret

    .sign:
    inc rcx
    lea rdi, [rdi + rcx]
    push r11
    call parse_uint
    pop r11
    

    cmp rdx, 0
    jz .error
    inc rdx
    
    cmp r11b, 0x2D
    jz .neg
    ret

    .neg:
        neg rax
        ret

    .error:
        ret

; Принимает указатель на строку, указатель на буфер и длину буфера
; Копирует строку в буфер
; Возвращает длину строки если она умещается в буфер, иначе 0
string_copy:
    xor rax, rax
    ret

