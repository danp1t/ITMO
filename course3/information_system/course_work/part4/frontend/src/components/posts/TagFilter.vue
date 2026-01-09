<template>
  <div class="tag-filter box mb-4">
    <h2 class="subtitle">Теги</h2>
    <div class="field">
      <div class="control">
        <div class="tags">
          <span
            v-for="tag in tags"
            :key="tag.id"
            class="tag is-clickable mr-1 mb-1"
            :class="{ 'is-primary is-light': selectedTagIds.includes(tag.id) }"
            @click="$emit('tag-toggle', tag.id)"
            :style="getTagStyle(tag)"
            :title="tag.description"
          >
            {{ tag.name }}
            <span v-if="tag.postCount" class="tag-count ml-1">
              ({{ tag.postCount }})
            </span>
          </span>
        </div>
      </div>
    </div>

    <div v-if="selectedTagIds.length > 0" class="mt-3">
      <button class="button is-small is-fullwidth" @click="$emit('clear')">
        <span class="icon">
          <i class="fas fa-times"></i>
        </span>
        <span>Сбросить фильтры</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Tag } from '@/types/posts'

defineProps<{
  tags: Tag[]
  selectedTagIds: number[]
}>()

defineEmits<{
  'tag-toggle': [tagId: number]
  'clear': []
}>()

const getTagStyle = (tag: Tag) => {
  const hue = (tag.id * 137) % 360
  return {
    backgroundColor: `hsl(${hue}, 70%, 95%)`,
    color: `hsl(${hue}, 50%, 30%)`,
    border: `1px solid hsl(${hue}, 60%, 85%)`
  }
}
</script>

<style scoped>
.tag-filter {
  background: #1a1a1a;
  border: 1px solid #2d2d2d;
}

.subtitle {
  color: #ffffff;
  font-weight: 600;
  margin-bottom: 1rem;
}

.tag {
  transition: all 0.2s ease;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.85em;
}

.tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.tag.is-primary.is-light {
  font-weight: 600;
  box-shadow: 0 0 0 2px rgba(50, 115, 220, 0.25);
}

.tag-count {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  padding: 0 6px;
  font-size: 0.85em;
  margin-left: 4px;
}

.button.is-small {
  background: #333;
  color: #ccc;
  border: 1px solid #404040;
}

.button.is-small:hover {
  background: #404040;
  color: white;
}
</style>
