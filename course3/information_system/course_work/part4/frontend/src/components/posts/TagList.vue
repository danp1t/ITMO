<template>
  <div class="tag-list">
    <div class="tags are-medium">
      <router-link
        v-for="tag in tags"
        :key="tag.id"
        :to="`/posts?tag=${tag.id}`"
        class="tag is-clickable"
        :style="getTagStyle(tag)"
        :title="tag.description"
      >
        {{ tag.name }}
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Tag } from '@/types/posts'

defineProps<{
  tags: Tag[]
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
.tag-list {
  margin-top: 1rem;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  margin-right: 8px;
  margin-bottom: 8px;
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.2s ease;
  text-decoration: none;
}

.tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}
</style>
