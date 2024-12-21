#define _DEFAULT_SOURCE

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "mem_internals.h"
#include "mem.h"
#include "util.h"

void debug_block(struct block_header* b, const char* fmt, ... );
void debug(const char* fmt, ... );

extern inline block_size size_from_capacity( block_capacity cap );
extern inline block_capacity capacity_from_size( block_size sz );

static bool            block_is_big_enough( size_t query, struct block_header* block ) { return block->capacity.bytes >= query; }
static size_t          pages_count   ( size_t mem )                      { return mem / getpagesize() + ((mem % getpagesize()) > 0); }
static size_t          round_pages   ( size_t mem )                      { return getpagesize() * pages_count( mem ) ; }

static void block_init( void* restrict addr, block_size block_sz, void* restrict next ) {
  *((struct block_header*)addr) = (struct block_header) {
    .next = next,
    .capacity = capacity_from_size(block_sz),
    .is_free = true
  };
}

static size_t region_actual_size( size_t query ) { return size_max( round_pages( query ), REGION_MIN_SIZE ); }

extern inline bool region_is_invalid( const struct region* r );



static void* map_pages(void const* addr, size_t length, int additional_flags) {
  return mmap( (void*) addr, length, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS | additional_flags , -1, 0 );
}

/*  аллоцировать регион памяти и инициализировать его блоком */
static struct region alloc_region  ( void const* addr, size_t query ) {
    struct region my_region;
    size_t length = region_actual_size(size_from_capacity((block_capacity){query}).bytes);

    void* my_addr = map_pages(addr, length, MAP_FIXED_NOREPLACE);

    if (my_addr == MAP_FAILED) {
        my_addr = map_pages(addr, length, 0);
        if (my_addr == MAP_FAILED) {
            return REGION_INVALID;
        }
        my_region.extends = 0;
    } else {
        my_region.extends = 1;
    }

    my_region.addr = my_addr;
    my_region.size = length;

    block_init(my_region.addr, (block_size){length}, NULL);
    return my_region;
}


void* heap_init( size_t initial ) {
  const struct region region = alloc_region( HEAP_START, initial );
  if ( region_is_invalid(&region) ) return NULL;

  return region.addr;
}

static void* block_after( struct block_header const* block )              {
    return  (void*) (block->contents + block->capacity.bytes);
}
static bool blocks_continuous (
        struct block_header const* fst,
        struct block_header const* snd ) {
    return (void*)snd == block_after(fst);
}

/*  освободить всю память, выделенную под кучу */
void heap_term() {
    struct block_header* current_region = HEAP_START;
    struct block_header* current_block = HEAP_START;

    while (current_block != NULL) {
        struct block_header* start = current_block;
        size_t size_to_unmap = 0;

        do {
            size_to_unmap += size_from_capacity(start->capacity).bytes;
            current_block = current_block->next;
        } while (current_block != NULL && blocks_continuous(start, current_block));

        if (munmap(current_region, size_to_unmap) == -1) {
            perror("Ошибка: не удалось освободить память");
        }
        current_region = current_block;
    }
}



#define BLOCK_MIN_CAPACITY 24

/*  --- Разделение блоков (если найденный свободный блок слишком большой )--- */

static bool block_splittable( struct block_header* restrict block, size_t query) {
  return block-> is_free && query + offsetof( struct block_header, contents ) + BLOCK_MIN_CAPACITY <= block->capacity.bytes;
}

static bool split_if_too_big(struct block_header* block, size_t query_size) {
    if (!block_splittable(block, query_size)) {
        return false;
    }
    size_t base_size = size_from_capacity((block_capacity){0}).bytes;
    size_t available_space = block->capacity.bytes - base_size;
    if (available_space < query_size + BLOCK_MIN_CAPACITY) {
        return false;
    }

    uint8_t* contents_ptr = (uint8_t*)block->contents;
    struct block_header* new_block = (struct block_header*)(contents_ptr + query_size);
    block_capacity new_capacity = { .bytes = available_space - query_size };
    block_init(new_block, size_from_capacity(new_capacity), block->next);
    block->next = new_block;
    block->capacity.bytes = query_size;
    return true;
}


/*  --- Слияние соседних свободных блоков --- */



static bool mergeable(struct block_header const* restrict fst, struct block_header const* restrict snd) {
  return fst->is_free && snd->is_free && blocks_continuous( fst, snd ) ;
}

static bool try_merge_with_next( struct block_header* block ) {
    if (block->next == NULL) return false;
    struct block_header* target_block = block->next;
    if (!mergeable(block, target_block)) {
        return false;
    }
    struct block_header* next_next = target_block->next;
    block->next = next_next;
    block->capacity.bytes += size_from_capacity(target_block->capacity).bytes;
    return true;
}


/*  --- ... ecли размера кучи хватает --- */

struct block_search_result {
  enum {BSR_FOUND_GOOD_BLOCK, BSR_REACHED_END_NOT_FOUND, BSR_CORRUPTED} type;
  struct block_header* block;
};


static struct block_search_result find_good_or_last  ( struct block_header* restrict block, size_t sz )    {
    if (block == NULL)return (struct block_search_result){BSR_CORRUPTED, block};
    while(true){
        if(block->is_free){
            while(try_merge_with_next(block));
            if(block->capacity.bytes >= sz)return (struct block_search_result) {BSR_FOUND_GOOD_BLOCK, block};
        }
        if(block->next == NULL)break;
        block = block->next;
    }
    return (struct block_search_result){BSR_REACHED_END_NOT_FOUND, block};
}

/*  Попробовать выделить память в куче начиная с блока `block` не пытаясь расширить кучу
 Можно переиспользовать как только кучу расширили. */
static struct block_search_result try_memalloc_existing ( size_t query, struct block_header* block ) {

    struct block_search_result search_result = find_good_or_last(block, query);

    if (search_result.type == BSR_FOUND_GOOD_BLOCK) {

        split_if_too_big(search_result.block, query);
        search_result.block->is_free = false;
    }

    return search_result;
}

static struct block_header* grow_heap(struct block_header* restrict last, size_t query) {
    if (last == NULL) {
        return NULL;
    }

    struct region new_region = alloc_region(block_after(last), query);
    if (region_is_invalid(&new_region)) {
        return NULL;
    }

    struct block_header *new_memory_block = (struct block_header *)new_region.addr;

    last->next = new_memory_block;

    if (new_region.extends && try_merge_with_next(last)) {
        return last;
    }

    return new_memory_block;
}


/*  Реализует основную логику malloc и возвращает заголовок выделенного блока */
static struct block_header* memalloc(size_t query, struct block_header* heap_start) {
    size_t requested_size = (query > BLOCK_MIN_CAPACITY) ? query : BLOCK_MIN_CAPACITY;

    struct block_search_result res = try_memalloc_existing(requested_size, heap_start);

    while (res.type == BSR_REACHED_END_NOT_FOUND) {
        heap_start = grow_heap(res.block, requested_size);
        if (heap_start == NULL) {
            return NULL;
        }
        res = try_memalloc_existing(requested_size, heap_start);
    }

    if (res.type == BSR_FOUND_GOOD_BLOCK) {
        return res.block;
    } else {
        return NULL;
    }
}

void* _malloc( size_t query ) {
  struct block_header* const addr = memalloc( query, (struct block_header*) HEAP_START );
  if (addr) return addr->contents;
  else return NULL;
}

static struct block_header* block_get_header(void* contents) {
  return (struct block_header*) (((uint8_t*)contents)-offsetof(struct block_header, contents));
}

void _free(void* mem) {
    if (!mem) return;

    struct block_header* header = block_get_header(mem);
    header->is_free = true;

    try_merge_with_next(header);
}
