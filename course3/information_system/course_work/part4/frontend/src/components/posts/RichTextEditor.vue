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

      <!-- Новая группа для медиа -->
      <div class="toolbar-group">
        <button
          @click="uploadFile"
          class="toolbar-button"
          title="Вставить файл"
          :disabled="uploadingFile"
        >
          <i class="fas fa-file"></i>
          <span v-if="uploadingFile" class="icon">
            <i class="fas fa-spinner fa-spin ml-1"></i>
          </span>
        </button>
        <button
          @click="uploadAudio"
          class="toolbar-button"
          title="Вставить аудио"
          :disabled="uploadingAudio"
        >
          <i class="fas fa-music"></i>
          <span v-if="uploadingAudio" class="icon">
            <i class="fas fa-spinner fa-spin ml-1"></i>
          </span>
        </button>
        <button
          @click="insertVideo"
          class="toolbar-button"
          title="Вставить видео"
        >
          <i class="fas fa-video"></i>
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

    <!-- Модальное окно для вставки видео -->
    <div v-if="showVideoModal" class="modal-overlay" @click="closeVideoModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Вставка видео</h3>
          <button class="modal-close" @click="closeVideoModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="video-source-selector">
            <label>
              <input
                type="radio"
                v-model="videoSource"
                value="youtube"
              > YouTube
            </label>
            <label>
              <input
                type="radio"
                v-model="videoSource"
                value="rutube"
              > RuTube
            </label>
          </div>

          <div class="video-input-group">
            <label for="videoUrl">Ссылка на видео:</label>
            <input
              id="videoUrl"
              v-model="videoUrl"
              type="text"
              placeholder="https://www.youtube.com/watch?v=..."
              @keyup.enter="insertVideoFromUrl"
            />
            <div class="video-examples">
              <small>Примеры:</small>
              <ul>
                <li v-if="videoSource === 'youtube'">https://www.youtube.com/watch?v=dQw4w9WgXcQ</li>
                <li v-if="videoSource === 'youtube'">https://youtu.be/dQw4w9WgXcQ</li>
                <li v-if="videoSource === 'rutube'">https://rutube.ru/video/...</li>
              </ul>
            </div>
          </div>

          <div class="video-preview" v-if="videoEmbedUrl">
            <div class="video-embed">
              <iframe
                width="100%"
                height="200"
                :src="videoEmbedUrl"
                frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen
              ></iframe>
            </div>
          </div>

          <div class="modal-actions">
            <button @click="closeVideoModal" class="btn-secondary">Отмена</button>
            <button
              @click="insertVideoFromUrl"
              class="btn-primary"
              :disabled="!videoEmbedUrl"
            >
              Вставить
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import Image from '@tiptap/extension-image'
import { computed, onBeforeUnmount, watch, ref } from 'vue'
import { Node } from '@tiptap/core'

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

// Состояние для модального окна видео
const showVideoModal = ref(false)
const videoUrl = ref('')
const videoSource = ref<'youtube' | 'rutube'>('youtube')

// Состояние для загрузки
const uploadingImage = ref(false)
const uploadingFile = ref(false)
const uploadingAudio = ref(false)

// Определение расширения для Iframe (видео)
const IframeExtension = Node.create({
  name: 'iframe',
  group: 'block',
  atom: true,

  addAttributes() {
    return {
      src: {
        default: null,
      },
      width: {
        default: '100%',
      },
      height: {
        default: '400px',
      },
      title: {
        default: 'Embedded content',
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'div[class="editor-iframe"] iframe',
        getAttrs: (dom) => ({
          src: (dom as HTMLIFrameElement).getAttribute('src'),
          width: (dom as HTMLIFrameElement).getAttribute('width') || '100%',
          height: (dom as HTMLIFrameElement).getAttribute('height') || '400px',
          title: (dom as HTMLIFrameElement).getAttribute('title') || 'Embedded content',
        }),
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return [
      'div',
      { class: 'editor-iframe' },
      [
        'iframe',
        {
          ...HTMLAttributes,
          frameborder: '0',
          allowfullscreen: 'true',
        },
      ],
    ]
  },

  addCommands() {
    return {
      setIframe:
        (options) =>
          ({ commands }) => {
            return commands.insertContent({
              type: this.name,
              attrs: options,
            })
          },
    }
  },
})

// Функция для определения иконки файла
const getFileIcon = (mimeType: string): string => {
  if (!mimeType) return 'file'

  if (mimeType.includes('pdf')) return 'file-pdf'
  if (mimeType.includes('word') || mimeType.includes('document')) return 'file-word'
  if (mimeType.includes('excel') || mimeType.includes('spreadsheet')) return 'file-excel'
  if (mimeType.includes('powerpoint') || mimeType.includes('presentation')) return 'file-powerpoint'
  if (mimeType.includes('zip') || mimeType.includes('archive')) return 'file-archive'
  if (mimeType.includes('image')) return 'file-image'
  if (mimeType.includes('audio')) return 'file-audio'
  if (mimeType.includes('video')) return 'file-video'
  if (mimeType.includes('text')) return 'file-alt'

  return 'file'
}

// Определение расширения для файлов
const FileExtension = Node.create({
  name: 'file',
  group: 'inline',
  inline: true,
  draggable: true,

  addAttributes() {
    return {
      src: {
        default: null,
      },
      name: {
        default: 'Файл',
      },
      size: {
        default: null,
      },
      type: {
        default: null,
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'a[class="editor-file"]',
        getAttrs: (dom) => ({
          src: (dom as HTMLLinkElement).getAttribute('href'),
          name: dom.textContent?.replace(/\s*\([^)]*\)$/, ''), // Удаляем размер из текста
          size: (dom as HTMLLinkElement).getAttribute('data-size'),
          type: (dom as HTMLLinkElement).getAttribute('data-type'),
        }),
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    const icon = getFileIcon(HTMLAttributes.type)

    return [
      'a',
      {
        class: 'editor-file',
        href: HTMLAttributes.src,
        download: HTMLAttributes.name,
        'data-size': HTMLAttributes.size,
        'data-type': HTMLAttributes.type,
      },
      [
        'i',
        { class: `fas fa-${icon}` },
      ],
      ` ${HTMLAttributes.name}${HTMLAttributes.size ? ` (${HTMLAttributes.size})` : ''}`,
    ]
  },

  addCommands() {
    return {
      setFile:
        (options) =>
          ({ commands }) => {
            return commands.insertContent({
              type: this.name,
              attrs: options,
            })
          },
    }
  },
})

// Определение расширения для аудио
const AudioExtension = Node.create({
  name: 'audio',
  group: 'block',
  atom: true,

  addAttributes() {
    return {
      src: {
        default: null,
      },
      title: {
        default: 'Аудио',
      },
      controls: {
        default: true,
      },
      autoplay: {
        default: false,
      },
      loop: {
        default: false,
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'audio',
        getAttrs: (dom) => ({
          src: (dom as HTMLAudioElement).getAttribute('src'),
          title: (dom as HTMLAudioElement).getAttribute('title') || 'Аудио',
          controls: (dom as HTMLAudioElement).hasAttribute('controls'),
          autoplay: (dom as HTMLAudioElement).hasAttribute('autoplay'),
          loop: (dom as HTMLAudioElement).hasAttribute('loop'),
        }),
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return [
      'audio',
      {
        ...HTMLAttributes,
        class: 'editor-audio',
        controls: HTMLAttributes.controls !== false,
      },
    ]
  },

  addCommands() {
    return {
      setAudio:
        (options) =>
          ({ commands }) => {
            return commands.insertContent({
              type: this.name,
              attrs: options,
            })
          },
    }
  },
})

// Создаем редактор с новыми расширениями
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
    IframeExtension, // Добавляем поддержку iframe
    FileExtension,   // Добавляем поддержку файлов
    AudioExtension,  // Добавляем поддержку аудио
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

// Счетчик символов
const characterCount = computed(() => {
  return editor.value?.storage.characterCount?.characters() || 0
})

// Получение embed URL для видео
const videoEmbedUrl = computed(() => {
  if (!videoUrl.value) return null

  const url = videoUrl.value.trim()

  if (videoSource.value === 'youtube') {
    // Обработка YouTube ссылок
    const youtubeRegex = /(?:youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]{11})/
    const match = url.match(youtubeRegex)

    if (match) {
      const videoId = match[1]
      return `https://www.youtube.com/embed/${videoId}`
    }
  } else if (videoSource.value === 'rutube') {
    // Обработка RuTube ссылок
    const rutubeRegex = /rutube\.ru\/video\/([a-zA-Z0-9_-]+)/
    const match = url.match(rutubeRegex)

    if (match) {
      const videoId = match[1]
      return `https://rutube.ru/play/embed/${videoId}`
    }
  }

  return null
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

    if (file.size > 5 * 1024 * 1024) {
      alert('Размер файла не должен превышать 5MB')
      return
    }

    uploadingImage.value = true

    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('postId', '1') // TODO: Замените на реальный postId
      formData.append('typeAttachmentId', '1')

      // Используйте полный URL к бэкенду
      const response = await fetch('http://localhost:8080/api/attachments/upload', {
        method: 'POST',
        body: formData
        // Не добавляйте заголовок Content-Type - FormData сам его установит
      })

      if (response.ok) {
        const attachment = await response.json()
        const fileUrl = `http://localhost:8080/api/attachments/${attachment.id}/download`

        editor.value
          ?.chain()
          .focus()
          .setImage({ src: fileUrl })
          .run()
      } else {
        const error = await response.text()
        console.error('Ошибка загрузки:', error)
        alert('Не удалось загрузить изображение')
      }
    } catch (error) {
      console.error('Ошибка загрузки изображения:', error)
      alert('Не удалось загрузить изображение')
    } finally {
      uploadingImage.value = false
    }
  }

  input.click()
}

// Общая функция загрузки файла
const uploadFileHandler = async (type: 'file' | 'audio') => {
  if (!editor.value) return

  const input = document.createElement('input')
  input.type = 'file'

  if (type === 'audio') {
    input.accept = 'audio/*'
    uploadingAudio.value = true
  } else {
    input.accept = '*/*'
    uploadingFile.value = true
  }

  input.onchange = async (e) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (!file) {
      resetUploadState(type)
      return
    }

    if (file.size > 10 * 1024 * 1024) {
      alert('Размер файла не должен превышать 10MB')
      resetUploadState(type)
      return
    }

    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('postId', '3') // TODO: Замените на реальный postId
      formData.append('typeAttachmentId', type === 'audio' ? '3' : '2')

      // Используйте полный URL к бэкенду
      const response = await fetch('http://localhost:8080/api/attachments/upload', {
        method: 'POST',
        body: formData
      })

      if (response.ok) {
        const attachment = await response.json()
        const fileUrl = `http://localhost:8080/api/attachments/${attachment.id}/download`

        if (type === 'audio') {
          editor.value
            ?.chain()
            .focus()
            .setAudio({ src: fileUrl, title: file.name })
            .run()
        } else {
          editor.value
            ?.chain()
            .focus()
            .setFile({
              src: fileUrl,
              name: file.name,
              size: formatFileSize(file.size),
              type: file.type
            })
            .run()
        }
      } else {
        const error = await response.text()
        console.error('Ошибка загрузки:', error)
        alert(`Не удалось загрузить ${type === 'audio' ? 'аудио' : 'файл'}`)
      }
    } catch (error) {
      console.error(`Ошибка загрузки ${type === 'audio' ? 'аудио' : 'файла'}:`, error)
      alert(`Не удалось загрузить ${type === 'audio' ? 'аудио' : 'файл'}`)
    } finally {
      resetUploadState(type)
    }
  }

  input.click()
}

const uploadFile = () => uploadFileHandler('file')
const uploadAudio = () => uploadFileHandler('audio')

const resetUploadState = (type: 'file' | 'audio') => {
  if (type === 'audio') {
    uploadingAudio.value = false
  } else {
    uploadingFile.value = false
  }
}

// Форматирование размера файла
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// Вставка видео
const insertVideo = () => {
  showVideoModal.value = true
  videoUrl.value = ''
}

// Вставка видео из URL
const insertVideoFromUrl = () => {
  if (!editor.value || !videoEmbedUrl.value) return

  editor.value
    .chain()
    .focus()
    .setIframe({ src: videoEmbedUrl.value })
    .run()

  closeVideoModal()
}

// Закрытие модального окна
const closeVideoModal = () => {
  showVideoModal.value = false
  videoUrl.value = ''
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
  color: #4a4a4a;
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
  color: #d8d8d8;
  border-radius: 0 0 4px 4px;
}

/* Модальное окно */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #272626;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 20px;
}

.video-source-selector {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.video-source-selector label {
  display: flex;
  align-items: center;
  gap: 5px;
}

.video-input-group {
  margin-bottom: 20px;
}

.video-input-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.video-input-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.video-examples {
  margin-top: 10px;
  color: #b3b3b3;
  font-size: 12px;
}

.video-examples ul {
  margin: 5px 0 0 20px;
  padding: 0;
}

.video-examples li {
  margin-bottom: 2px;
}

.video-preview {
  margin: 20px 0;
  padding: 10px;
  background: #e6e1e1;
  border-radius: 4px;
}

.video-embed {
  position: relative;
  padding-bottom: 56.25%; /* 16:9 Aspect Ratio */
  height: 0;
}

.video-embed iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-primary,
.btn-secondary {
  padding: 8px 16px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background: #3273dc;
  color: white;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f5f5f5;
  color: #333;
}

/* Стили для медиа-элементов в редакторе */
:deep(.editor-image) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 10px 0;
}

:deep(.editor-audio) {
  width: 100%;
  max-width: 500px;
  margin: 10px 0;
  border-radius: 4px;
}

:deep(.editor-file) {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 4px;
  margin: 5px 0;
  text-decoration: none;
  color: #333;
  border: 1px solid #ddd;
}

:deep(.editor-file:hover) {
  background: #e9e9e9;
  text-decoration: none;
}

:deep(.editor-file i) {
  margin-right: 8px;
  color: #666;
}

:deep(.editor-iframe) {
  width: 100%;
  max-width: 100%;
  margin: 20px 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

:deep(.editor-iframe iframe) {
  width: 100%;
  height: 400px;
  border: none;
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
