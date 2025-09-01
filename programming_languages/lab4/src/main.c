//
// Created by danp1t on 15.12.2024.
//

#include <assert.h>
#include <stdio.h>

#include "mem.h"

void test_successful_allocation() {
    printf("Запуск теста: Выделение памяти\n");
    heap_init(1024);
    void* ptr = _malloc(256);
    if (ptr != NULL) {
        printf("Память успешно выделена: %p\n", ptr);
    } else {
        printf("Не смогли выделить память\n");
    }
    heap_term();
}

void test_free_one_block() {
    printf("Запуск теста: Освобождение одного блока из нескольких выделенных\n");
    heap_init(1024);
    void* ptr1 = _malloc(256);
    printf("Выделен блок: %p (256 байт)\n", ptr1);
    void* ptr2 = _malloc(256);
    printf("Выделен блок: %p (256 байт)\n", ptr2);
    _free(ptr1);
    printf("Освободили один блок: %p\n", ptr1);
    heap_term();
}

void test_free_two_blocks() {
    printf("Запуск теста: Освобождение двух блоков из нескольких выделенных\n");
    heap_init(1024);
    void* ptr1 = _malloc(256);
    void* ptr2 = _malloc(256);
    _free(ptr1);
    _free(ptr2);
    printf("Освобождение двух блоков: %p, %p\n", ptr1, ptr2);
    heap_term();
}

void test_memory_expansion() {
    printf("Запуск теста: Успешное расширение старого региона памяти\n");
    heap_init(1024);
    void* ptr1 = _malloc(512);
    assert(ptr1 != NULL);
    printf("Выделен блок: %p (512 байт)\n", ptr1);
    void* ptr2 = _malloc(512);
    assert(ptr2 != NULL);
    printf("Выделен блок: %p (512 байт)\n", ptr2);
    _free(ptr1);
    printf("Освобожден блок: %p\n", ptr1);
    void* ptr3 = _malloc(256);
    assert(ptr3 != NULL);
    printf("Выделен блок: %p (256 байт)\n", ptr3);
    _free(ptr2);
    printf("Освобожден блок: %p\n", ptr2);
    _free(ptr3);
    printf("Освобожден блок: %p\n", ptr3);
    heap_term();
}

void test_memory_expansion_failure() {
    printf("Запуск теста: Неуспешное расширение старого региона памяти\n");
    heap_init(1024);

    void* ptr1 = _malloc(512);
    assert(ptr1 != NULL);
    printf("Выделен блок: %p (512 байт)\n", ptr1);
    void* ptr2 = _malloc(512);
    assert(ptr2 != NULL);
    printf("Выделен блок: %p (512 байт)\n", ptr2);
    _free(ptr1);
    printf("Освобожден блок: %p\n", ptr1);
    void* ptr3 = _malloc(1024);
    assert(ptr3 != NULL);
    printf("Выделен большой блок: %p (1024 байта)\n", ptr3);
    _free(ptr2);
    printf("Освобожден блок: %p\n", ptr2);
    void* ptr4 = _malloc(256);
    assert(ptr4 == NULL);
    printf("Попытка выделить блок: %p (256 байт) - не удалось, память закончилась.\n", ptr4);
    _free(ptr3);
    printf("Освобожден большой блок: %p\n", ptr3);
    heap_term();
}




int main() {
    test_successful_allocation();
    test_free_one_block();
    test_free_two_blocks();
    test_memory_expansion();
    test_memory_expansion_failure();

    return 0;
}
