<template>
  <div class="tag-selector">
    <div class="selected-tags mb-3">
      <span
        v-for="tag in selectedTags"
        :key="tag.id"
        class="tag is-medium mr-2 mb-2"
        :style="getTagStyle(tag)"
      >
        {{ tag.name }}
        <button
          class="delete is-small ml-2"
          @click="removeTag(tag.id)"
        ></button>
      </span>
    </div>

    <div class="field">
      <div class="control has-icons-right">
        <input
          v-model="searchQuery"
          class="input"
          type="text"
          placeholder="Поиск тегов..."
          @input="onSearch"
          @focus="showDropdown = true"
          @blur="onBlur"
          maxlength="100"
        >
        <span class="icon is-right">
          <i class="fas fa-search"></i>
        </span>
      </div>
      <p class="help">Максимум 100 символов</p>
    </div>

    <div v-if="showDropdown" class="dropdown-menu is-block dark-dropdown">
      <div class="dropdown-content">
        <div
          v-for="tag in filteredTags"
          :key="tag.id"
          class="dropdown-item is-flex is-align-items-center"
          @mousedown.prevent="selectTag(tag)"
        >
          <span
            class="tag mr-2"
            :style="getTagStyle(tag)"
          >
            {{ tag.name }}
          </span>
          <span class="tag-description is-size-7">
            {{ tag.description }}
          </span>
        </div>

        <div v-if="filteredTags.length === 0" class="dropdown-item">
          <p class="has-text-grey">Теги не найдены</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { tagsAPI } from '@/api/tags'
import type { Tag } from '@/types/posts'

interface Props {
  modelValue?: number[]
  postId?: number
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  postId: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: number[]]
  'change': [tags: Tag[]]
}>()

const allTags = ref<Tag[]>([])
const selectedTags = ref<Tag[]>([])
const searchQuery = ref('')
const showDropdown = ref(false)
const isLoading = ref(false)

const loadTags = async () => {
  try {
    const response = await tagsAPI.getAllTags()
    allTags.value = response.data || []
  } catch (error) {
    console.error('Ошибка при загрузке тегов:', error)
  }
}

const loadPostTags = async () => {
  if (!props.postId) return

  if (isLoading.value) return
  isLoading.value = true

  try {
    const response = await tagsAPI.getPostTags(props.postId)
    selectedTags.value = response.data || []
  } catch (error) {
    console.error('Ошибка при загрузке тегов поста:', error)
  } finally {
    isLoading.value = false
  }
}

const filteredTags = computed(() => {
  const query = searchQuery.value.substring(0, 100).toLowerCase()

  if (!query.trim()) {
    return allTags.value
      .filter(tag => !selectedTags.value.some(st => st.id === tag.id))
      .slice(0, 10)
  }

  return allTags.value
    .filter(tag =>
      tag.name.toLowerCase().includes(query) ||
      (tag.description && tag.description.toLowerCase().includes(query))
    )
    .filter(tag => !selectedTags.value.some(st => st.id === tag.id))
    .slice(0, 10)
})

const selectTag = async (tag: Tag) => {
  if (!selectedTags.value.some(t => t.id === tag.id)) {
    selectedTags.value.push(tag)
    const selectedIds = selectedTags.value.map(t => t.id)

    await nextTick()

    emit('update:modelValue', selectedIds)
    emit('change', selectedTags.value)

    searchQuery.value = ''
    showDropdown.value = false
  }
}

const removeTag = async (tagId: number) => {
  selectedTags.value = selectedTags.value.filter(t => t.id !== tagId)
  const selectedIds = selectedTags.value.map(t => t.id)
  await nextTick()

  emit('update:modelValue', selectedIds)
  emit('change', selectedTags.value)
}

const getTagStyle = (tag: Tag) => {
  const hue = (tag.id * 137) % 360
  return {
    backgroundColor: `hsl(${hue}, 70%, 90%)`,
    color: `hsl(${hue}, 50%, 30%)`,
    border: `1px solid hsl(${hue}, 60%, 80%)`
  }
}

const onSearch = () => {
  // Ограничиваем длину поискового запроса
  if (searchQuery.value.length > 100) {
    searchQuery.value = searchQuery.value.substring(0, 100)
  }
  showDropdown.value = true
}

const onBlur = () => {
  setTimeout(() => {
    showDropdown.value = false
  }, 200)
}

watch(() => props.postId, (newPostId) => {
  if (newPostId) {
    loadPostTags()
  }
}, { immediate: true })

watch(() => props.modelValue, (newIds) => {
  if (props.postId) {
    return
  }

  const currentIds = selectedTags.value.map(t => t.id)
  if (JSON.stringify(currentIds.sort()) !== JSON.stringify(newIds.slice().sort())) {
    selectedTags.value = allTags.value.filter(tag => newIds.includes(tag.id))
  }
}, { immediate: true })

onMounted(async () => {
  await loadTags()

  if (!props.postId && props.modelValue?.length) {
    selectedTags.value = allTags.value.filter(tag => props.modelValue.includes(tag.id))
  }
})
</script>

<style scoped>
.tag-selector {
  position: relative;
}

.dark-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1000;
  background: #1a1a1a;
  border: 1px solid #333;
  border-radius: 6px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  max-height: 300px;
  overflow-y: auto;
  margin-top: 4px;
}

.dropdown-content {
  padding: 8px 0;
}

.dropdown-item {
  cursor: pointer;
  padding: 10px 16px;
  color: #171313;
  transition: all 0.2s ease;
}

.dropdown-item:hover {
  background-color: #252525;
  color: #ffffff;
}

.tag-description {
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #a0a0a0;
}

.input {
  background-color: #252525;
  border-color: #404040;
  color: #ffffff;
}

.input:focus {
  border-color: #3273dc;
  box-shadow: 0 0 0 0.125em rgba(50, 115, 220, 0.25);
}

.input::placeholder {
  color: #888;
}

.icon {
  color: #888;
}

.popular-tags .tag {
  transition: all 0.2s;
  background-color: #2a2a2a;
  color: #ccc;
  border: 1px solid #404040;
}

.popular-tags .tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  background-color: #333;
  color: #fff;
}

.selected-tags .tag {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  margin-right: 10px;
  margin-bottom: 10px;
  border-radius: 20px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.selected-tags .delete {
  margin-left: 6px;
  opacity: 0.7;
  background-color: transparent;
  border: none;
  color: rgba(0, 0, 0, 0.5);
}

.selected-tags .delete:hover {
  opacity: 1;
  background-color: rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .dark-dropdown {
    max-height: 250px;
  }

  .dropdown-item {
    padding: 8px 12px;
  }
}
</style>
