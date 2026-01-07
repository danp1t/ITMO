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

// Реактивные состояния
const allTags = ref<Tag[]>([])
const selectedTags = ref<Tag[]>([])
const searchQuery = ref('')
const showDropdown = ref(false)
const isLoading = ref(false)

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

  if (isLoading.value) return // Предотвращаем повторные запросы
  isLoading.value = true

  try {
    const response = await tagsAPI.getPostTags(props.postId)
    selectedTags.value = response.data || []
    // НЕ вызываем emit здесь, чтобы избежать бесконечного цикла
  } catch (error) {
    console.error('Ошибка при загрузке тегов поста:', error)
  } finally {
    isLoading.value = false
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
const selectTag = async (tag: Tag) => {
  if (!selectedTags.value.some(t => t.id === tag.id)) {
    selectedTags.value.push(tag)

    // Сначала обновляем локальное состояние
    const selectedIds = selectedTags.value.map(t => t.id)

    // Используем nextTick для предотвращения циклов
    await nextTick()

    // Затем эмитируем изменения
    emit('update:modelValue', selectedIds)
    emit('change', selectedTags.value)

    searchQuery.value = ''
    showDropdown.value = false
  }
}

// Удаление тега
const removeTag = async (tagId: number) => {
  selectedTags.value = selectedTags.value.filter(t => t.id !== tagId)

  // Сначала обновляем локальное состояние
  const selectedIds = selectedTags.value.map(t => t.id)

  // Используем nextTick для предотвращения циклов
  await nextTick()

  // Затем эмитируем изменения
  emit('update:modelValue', selectedIds)
  emit('change', selectedTags.value)
}

// Стиль тега
const getTagStyle = (tag: Tag) => {
  const hue = (tag.id * 137) % 360
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

// Наблюдатель за изменениями postId
watch(() => props.postId, (newPostId) => {
  if (newPostId) {
    loadPostTags()
  }
}, { immediate: true })

// Наблюдатель за внешними изменениями modelValue
// Используем deep: false и только для случаев без postId
watch(() => props.modelValue, (newIds) => {
  // Если у нас есть postId, игнорируем внешние изменения modelValue
  // потому что теги загружаются с сервера
  if (props.postId) {
    return
  }

  // Только для новых постов синхронизируем с внешним значением
  // Проверяем, действительно ли изменились ID
  const currentIds = selectedTags.value.map(t => t.id)
  if (JSON.stringify(currentIds.sort()) !== JSON.stringify(newIds.slice().sort())) {
    selectedTags.value = allTags.value.filter(tag => newIds.includes(tag.id))
  }
}, { immediate: true })

onMounted(async () => {
  await loadTags()

  // Если нет postId, но есть начальные значения в modelValue
  if (!props.postId && props.modelValue?.length) {
    // Выбираем теги по ID
    selectedTags.value = allTags.value.filter(tag => props.modelValue.includes(tag.id))
  }
})
</script>

<style scoped>
.tag-selector {
  position: relative;
}

/* Темное выпадающее меню */
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

/* Поле ввода в темной теме */
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

/* Иконка поиска */
.icon {
  color: #888;
}

/* Популярные теги */
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

/* Выбранные теги */
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

/* Адаптивность */
@media (max-width: 768px) {
  .dark-dropdown {
    max-height: 250px;
  }

  .dropdown-item {
    padding: 8px 12px;
  }
}
</style>
