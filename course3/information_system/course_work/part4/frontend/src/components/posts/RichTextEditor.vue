<template>
  <div class="rich-text-editor">
    <!-- Панель инструментов -->
    <div v-if="editor" class="editor-toolbar">
      <div class="toolbar-group">
        <button
          @click="editor.chain().focus().toggleHeading({ level: 1 }).run()"
          :class="{ 'is-active': editor.isActive('heading', { level: 1 }) }"
          class="toolbar-button"
          title="Заголовок 1"
        >
          H1
        </button>
        <button
          @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
          :class="{ 'is-active': editor.isActive('heading', { level: 2 }) }"
          class="toolbar-button"
          title="Заголовок 2"
        >
          H2
        </button>
        <button
          @click="editor.chain().focus().toggleHeading({ level: 3 }).run()"
          :class="{ 'is-active': editor.isActive('heading', { level: 3 }) }"
          class="toolbar-button"
          title="Заголовок 3"
        >
          H3
        </button>
      </div>

      <div class="toolbar-group">
        <button
          @click="editor.chain().focus().toggleBold().run()"
          :class="{ 'is-active': editor.isActive('bold') }"
          class="toolbar-button"
          title="Жирный"
        >
          <i class="fas fa-bold"></i>
        </button>
        <button
          @click="editor.chain().focus().toggleItalic().run()"
          :class="{ 'is-active': editor.isActive('italic') }"
          class="toolbar-button"
          title="Курсив"
        >
          <i class="fas fa-italic"></i>
        </button>
        <button
          @click="editor.chain().focus().toggleStrike().run()"
          :class="{ 'is-active': editor.isActive('strike') }"
          class="toolbar-button"
          title="Зачеркнутый"
        >
          <i class="fas fa-strikethrough"></i>
        </button>
      </div>

      <div class="toolbar-group">
        <button
          @click="editor.chain().focus().toggleBulletList().run()"
          :class="{ 'is-active': editor.isActive('bulletList') }"
          class="toolbar-button"
          title="Маркированный список"
        >
          <i class="fas fa-list-ul"></i>
        </button>
        <button
          @click="editor.chain().focus().toggleOrderedList().run()"
          :class="{ 'is-active': editor.isActive('orderedList') }"
          class="toolbar-button"
          title="Нумерованный список"
        >
          <i class="fas fa-list-ol"></i>
        </button>
        <button
          @click="editor.chain().focus().toggleBlockquote().run()"
          :class="{ 'is-active': editor.isActive('blockquote') }"
          class="toolbar-button"
          title="Цитата"
        >
          <i class="fas fa-quote-right"></i>
        </button>
      </div>

      <div class="toolbar-group">
        <button
          @click="editor.chain().focus().setHorizontalRule().run()"
          class="toolbar-button"
          title="Горизонтальная линия"
        >
          <i class="fas fa-minus"></i>
        </button>
        <button
          @click="editor.chain().focus().setHardBreak().run()"
          class="toolbar-button"
          title="Разрыв строки"
        >
          <i class="fas fa-level-down-alt"></i>
        </button>
        <button
          @click="editor.chain().focus().undo().run()"
          class="toolbar-button"
          title="Отменить"
        >
          <i class="fas fa-undo"></i>
        </button>
        <button
          @click="editor.chain().focus().redo().run()"
          class="toolbar-button"
          title="Повторить"
        >
          <i class="fas fa-redo"></i>
        </button>
      </div>

      <!-- Кнопка загрузки изображения -->
      <div class="toolbar-group">
        <button
          @click="uploadImage"
          class="toolbar-button"
          title="Вставить изображение"
          :disabled="uploadingImage"
        >
          <i class="fas fa-image"></i>
          <span v-if="uploadingImage" class="icon">
            <i class="fas fa-spinner fa-spin ml-1"></i>
          </span>
        </button>
      </div>
    </div>

    <!-- Область редактирования -->
    <editor-content
      :editor="editor"
      class="editor-content"
      :class="{ 'is-disabled': disabled }"
    />

    <!-- Счетчик символов -->
    <div v-if="showCounter" class="character-counter">
      Символов: {{ characterCount }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import Image from '@tiptap/extension-image'
import { computed, onBeforeUnmount, watch, ref } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    placeholder?: string
    disabled?: boolean
    showCounter?: boolean
  }>(),
  {
    modelValue: '',
    placeholder: 'Начните писать...',
    disabled: false,
    showCounter: true,
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// Создаем редактор
const editor = useEditor({
  content: props.modelValue || '',
  extensions: [
    StarterKit,
    Image.configure({
      inline: true,
      allowBase64: false,
      HTMLAttributes: {
        class: 'editor-image',
      },
    }),
    Placeholder.configure({
      placeholder: props.placeholder,
    }),
  ],
  editorProps: {
    attributes: {
      class: 'prose prose-sm max-w-none focus:outline-none',
    },
  },
  editable: !props.disabled,
  onUpdate: ({ editor }) => {
    const html = editor.getHTML()
    emit('update:modelValue', html)
  },
})

// Состояние для загрузки изображения
const uploadingImage = ref(false)

// Счетчик символов
const characterCount = computed(() => {
  return editor.value?.storage.characterCount?.characters() || 0
})

// Загрузка изображения
const uploadImage = async () => {
  if (!editor.value || uploadingImage.value) return

  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'

  input.onchange = async (e) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (!file) return

    // Проверка размера файла (макс 5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('Размер файла не должен превышать 5MB')
      return
    }

    uploadingImage.value = true

    try {
      // Здесь должен быть вызов вашего API для загрузки вложений
      // Пример:
      // const formData = new FormData()
      // formData.append('file', file)
      // const response = await attachmentService.upload(formData)
      // const imageUrl = response.data.path

      // Для демо используем Data URL
      const reader = new FileReader()
      reader.onload = (e) => {
        const result = e.target?.result as string

        // Вставляем изображение в редактор
        editor.value
          ?.chain()
          .focus()
          .setImage({ src: result })
          .run()
      }
      reader.readAsDataURL(file)
    } catch (error) {
      console.error('Ошибка загрузки изображения:', error)
      alert('Не удалось загрузить изображение')
    } finally {
      uploadingImage.value = false
    }
  }

  input.click()
}

// Наблюдаем за изменением modelValue
watch(() => props.modelValue, (newValue) => {
  if (editor.value && newValue !== editor.value.getHTML()) {
    editor.value.commands.setContent(newValue || '', false)
  }
})

// Наблюдаем за disabled
watch(() => props.disabled, (newValue) => {
  if (editor.value) {
    editor.value.setEditable(!newValue)
  }
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})

// Экспортируем методы для использования извне
defineExpose({
  editor,
  clear: () => editor.value?.commands.clearContent(),
  focus: () => editor.value?.commands.focus(),
})
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid #dbdbdb;
  border-radius: 4px;
  background-color: #3a3636;
  transition: border-color 0.2s ease;
}

.rich-text-editor:focus-within {
  border-color: #3273dc;
  box-shadow: 0 0 0 0.125em rgba(50, 115, 220, 0.25);
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  padding: 8px 12px;
  border-bottom: 1px solid #dbdbdb;
  background-color: #191717;
  border-radius: 4px 4px 0 0;
}

.toolbar-group {
  display: flex;
  gap: 2px;
  padding-right: 10px;
  border-right: 1px solid #ddd;
}

.toolbar-group:last-child {
  border-right: none;
  padding-right: 0;
}

.toolbar-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: 1px solid #ddd;
  background: white;
  border-radius: 3px;
  cursor: pointer;
  font-size: 0.9em;
  color: #363636;
  transition: all 0.2s ease;
}

.toolbar-button:hover:not(:disabled) {
  background-color: #f0f0f0;
  border-color: #bbb;
}

.toolbar-button.is-active {
  background-color: #3273dc;
  color: white;
  border-color: #3273dc;
}

.toolbar-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.editor-content {
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
  padding: 15px;
  font-size: 1rem;
  line-height: 1.5;
}

.editor-content.is-disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.character-counter {
  padding: 8px 15px;
  border-top: 1px solid #dbdbdb;
  background-color: #302c2c;
  text-align: right;
  font-size: 0.8rem;
  color: #666;
  border-radius: 0 0 4px 4px;
}

/* Стили для изображений в редакторе */
:deep(.editor-image) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 10px 0;
}

:deep(.ProseMirror:focus) {
  outline: none;
}

:deep(.ProseMirror p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  float: left;
  color: #adb5bd;
  pointer-events: none;
  height: 0;
}

/* Стили для различных элементов редактора */
:deep(.ProseMirror h1) {
  font-size: 2em;
  font-weight: 600;
  margin: 0.67em 0;
}

:deep(.ProseMirror h2) {
  font-size: 1.5em;
  font-weight: 600;
  margin: 0.75em 0;
}

:deep(.ProseMirror h3) {
  font-size: 1.17em;
  font-weight: 600;
  margin: 0.83em 0;
}

:deep(.ProseMirror ul),
:deep(.ProseMirror ol) {
  padding-left: 1.5em;
  margin: 1em 0;
}

:deep(.ProseMirror blockquote) {
  border-left: 3px solid #ddd;
  padding-left: 1em;
  margin: 1em 0;
  color: #666;
}

:deep(.ProseMirror hr) {
  border: none;
  border-top: 2px solid #ddd;
  margin: 2em 0;
}
</style>
