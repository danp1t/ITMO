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
        >
        <span class="icon is-right">
          <i class="fas fa-search"></i>
        </span>
      </div>
    </div>

    <div v-if="showDropdown" class="dropdown-menu is-block">
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
          <span class="tag-description is-size-7 has-text-grey">
            {{ tag.description }}
          </span>
        </div>

        <div v-if="filteredTags.length === 0" class="dropdown-item">
          <p class="has-text-grey">Теги не найдены</p>
        </div>
      </div>
    </div>

    <div v-if="popularTags.length > 0" class="popular-tags mt-4">
      <p class="is-size-7 has-text-grey mb-2">Популярные теги:</p>
      <div class="tags">
        <span
          v-for="tag in popularTags"
          :key="tag.id"
          class="tag is-light mr-1 mb-1 is-clickable"
          @click="selectTag(tag)"
          :title="tag.description"
        >
          {{ tag.name }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue' // ДОБАВЛЕНО watch
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

// Реактивные состояния
const allTags = ref<Tag[]>([])
const selectedTags = ref<Tag[]>([])
const searchQuery = ref('')
const showDropdown = ref(false)

// Загрузка всех тегов
const loadTags = async () => {
  try {
    const response = await tagsAPI.getAllTags()
    allTags.value = response.data || []
  } catch (error) {
    console.error('Ошибка при загрузке тегов:', error)
  }
}

// Загрузка тегов поста
const loadPostTags = async () => {
  if (!props.postId) return

  try {
    const response = await tagsAPI.getPostTags(props.postId)
    selectedTags.value = response.data || []
    emit('update:modelValue', selectedTags.value.map(t => t.id))
  } catch (error) {
    console.error('Ошибка при загрузке тегов поста:', error)
  }
}

// Отфильтрованные теги для поиска
const filteredTags = computed(() => {
  if (!searchQuery.value.trim()) {
    return allTags.value
      .filter(tag => !selectedTags.value.some(st => st.id === tag.id))
      .slice(0, 10)
  }

  return allTags.value
    .filter(tag =>
      tag.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      (tag.description && tag.description.toLowerCase().includes(searchQuery.value.toLowerCase()))
    )
    .filter(tag => !selectedTags.value.some(st => st.id === tag.id))
    .slice(0, 10)
})

// Популярные теги (первые 8)
const popularTags = computed(() => {
  return allTags.value.slice(0, 8)
})

// Выбор тега
const selectTag = (tag: Tag) => {
  if (!selectedTags.value.some(t => t.id === tag.id)) {
    selectedTags.value.push(tag)
    emit('update:modelValue', selectedTags.value.map(t => t.id))
    emit('change', selectedTags.value)
    searchQuery.value = ''
  }
}

// Удаление тега
const removeTag = (tagId: number) => {
  selectedTags.value = selectedTags.value.filter(t => t.id !== tagId)
  emit('update:modelValue', selectedTags.value.map(t => t.id))
  emit('change', selectedTags.value)
}

// Стиль тега (можно использовать цвет из БД или генерировать)
const getTagStyle = (tag: Tag) => {
  // Пример: генерируем цвет на основе id
  const hue = (tag.id * 137) % 360 // Золотое сечение для распределения
  return {
    backgroundColor: `hsl(${hue}, 70%, 90%)`,
    color: `hsl(${hue}, 50%, 30%)`,
    border: `1px solid hsl(${hue}, 60%, 80%)`
  }
}

// Обработчик поиска
const onSearch = () => {
  showDropdown.value = true
}

// Обработчик потери фокуса
const onBlur = () => {
  setTimeout(() => {
    showDropdown.value = false
  }, 200)
}

// Наблюдатель за modelValue (внешними изменениями)
watch(() => props.modelValue, (newIds) => {
  if (props.postId) {
    // Для существующего поста загружаем теги по ID
    loadPostTags()
  } else {
    // Для нового поста используем только ID
    selectedTags.value = allTags.value.filter(tag => newIds.includes(tag.id))
  }
}, { immediate: true })

onMounted(async () => {
  await loadTags()

  // Если есть ID поста, загружаем его теги
  if (props.postId) {
    await loadPostTags()
  } else if (props.modelValue?.length) {
    // Если переданы ID тегов, выбираем их
    selectedTags.value = allTags.value.filter(tag => props.modelValue.includes(tag.id))
  }
})
</script>

<style scoped>
.tag-selector {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1000;
  background: white;
  border: 1px solid #dbdbdb;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  max-height: 300px;
  overflow-y: auto;
}

.dropdown-item {
  cursor: pointer;
  padding: 8px 12px;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
}

.tag-description {
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.popular-tags .tag {
  transition: all 0.2s;
}

.popular-tags .tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.selected-tags .tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  margin-right: 8px;
  margin-bottom: 8px;
  border-radius: 20px;
  font-weight: 500;
}

.selected-tags .delete {
  margin-left: 4px;
  opacity: 0.7;
}

.selected-tags .delete:hover {
  opacity: 1;
}
</style>
