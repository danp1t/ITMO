<template>
  <div class="modal" :class="{ 'is-active': isVisible }">
    <AppNotification
      :notification="notification"
      @hide="hideNotification"
    />

    <div class="modal-background" @click="$emit('close')"></div>

    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">Добавить товар</p>
        <button class="delete" aria-label="close" @click="$emit('close')"></button>
      </header>

      <section class="modal-card-body">
        <form @submit.prevent="submitForm">
          <div class="field">
            <label class="label">Название товара *</label>
            <div class="control">
              <input
                v-model="formData.name"
                class="input"
                type="text"
                required
                placeholder="Введите название товара"
                :disabled="isSubmitting"
                maxlength="100"
              >
            </div>
            <p class="help">Максимум 100 символов</p>
          </div>

          <div class="field">
            <label class="label">Описание *</label>
            <div class="control">
              <textarea
                v-model="formData.description"
                class="textarea"
                required
                placeholder="Введите описание товара"
                rows="3"
                :disabled="isSubmitting"
                maxlength="500"
              ></textarea>
            </div>
            <p class="help">Максимум 500 символов</p>
          </div>

          <div class="columns">
            <div class="column">
              <div class="field">
                <label class="label">Категория *</label>
                <div class="control">
                  <div class="select is-fullwidth">
                    <select v-model="formData.category" required :disabled="isSubmitting">
                      <option value="" disabled>Выберите категорию</option>
                      <option value="Купальники">Купальники</option>
                      <option value="Получешки">Получешки</option>
                      <option value="Булавы">Булавы</option>
                      <option value="Обручи">Обручи</option>
                      <option value="Мячи">Мячи</option>
                      <option value="Ленты">Ленты</option>
                      <option value="Скакалки">Скакалки</option>
                      <option value="Аксессуары">Аксессуары</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>

            <div class="column">
              <div class="field">
                <label class="label">Базовая цена (₽) *</label>
                <div class="control">
                  <input
                    v-model.number="formData.basePrice"
                    class="input"
                    type="number"
                    min="0"
                    max="1000000"
                    required
                    placeholder="0"
                    :disabled="isSubmitting"
                    step="1"
                  >
                </div>
                <p class="help">Максимум 1,000,000 ₽</p>
              </div>
            </div>
          </div>

          <div class="field">
            <label class="label">Изображения товара</label>
            <div class="image-upload-section">
              <div v-if="selectedImages.length > 0" class="image-preview-grid">
                <div
                  v-for="(image, index) in selectedImages"
                  :key="index"
                  class="image-preview-item"
                >
                  <img :src="getImageUrl(image)" :alt="`Изображение ${index + 1}`" class="preview-image">
                  <button
                    type="button"
                    class="delete-image-button"
                    @click="removeImage(index)"
                    :disabled="isSubmitting"
                  >
                    <i class="fas fa-times"></i>
                  </button>
                </div>
              </div>

              <div class="file is-boxed" :class="{ 'is-disabled': isSubmitting }">
                <label class="file-label" :class="{ 'is-disabled': isSubmitting }">
                  <input
                    ref="fileInput"
                    type="file"
                    multiple
                    accept="image/*"
                    @change="handleImageSelect"
                    class="file-input"
                    :disabled="isSubmitting"
                  >
                  <span class="file-cta" @click.stop="triggerFileInput">
                    <span class="file-icon">
                      <i class="fas fa-upload"></i>
                    </span>
                    <span class="file-label">
                      Выбрать изображения
                    </span>
                  </span>
                </label>
              </div>

              <p class="help">
                Можно загрузить несколько изображений. Максимальный размер: 5MB каждый.
                Поддерживаемые форматы: JPG, PNG, GIF.
              </p>
            </div>
          </div>

          <div class="field">
            <label class="label">Размеры и цены</label>

            <div
              v-for="(size, index) in formData.sizes"
              :key="index"
              class="box mb-3"
            >
              <div class="level is-mobile">
                <div class="level-left">
                  <h4 class="title is-6">Размер {{ index + 1 }}</h4>
                </div>
                <div class="level-right">
                  <button
                    type="button"
                    class="button is-danger is-small"
                    @click="removeSize(index)"
                    :disabled="formData.sizes.length <= 1 || isSubmitting"
                  >
                    Удалить
                  </button>
                </div>
              </div>

              <div class="columns">
                <div class="column">
                  <div class="field">
                    <label class="label">Название размера *</label>
                    <div class="control">
                      <input
                        v-model="size.sizeName"
                        class="input"
                        type="text"
                        required
                        placeholder="Например: S, M, L или 36, 38"
                        :disabled="isSubmitting"
                        maxlength="50"
                      >
                    </div>
                    <p class="help">Максимум 50 символов</p>
                  </div>
                </div>

                <div class="column">
                  <div class="field">
                    <label class="label">Цена (₽) *</label>
                    <div class="control">
                      <input
                        v-model.number="size.price"
                        class="input"
                        type="number"
                        min="0"
                        max="1000000"
                        required
                        placeholder="0"
                        :disabled="isSubmitting"
                        step="1"
                      >
                    </div>
                    <p class="help">Максимум 1,000,000 ₽</p>
                  </div>
                </div>

                <div class="column">
                  <div class="field">
                    <label class="label">Количество *</label>
                    <div class="control">
                      <input
                        v-model.number="size.countItems"
                        class="input"
                        type="number"
                        min="0"
                        max="10000"
                        required
                        placeholder="0"
                        :disabled="isSubmitting"
                        step="1"
                      >
                    </div>
                    <p class="help">Максимум 10,000 единиц</p>
                  </div>
                </div>
              </div>
            </div>

            <button
              type="button"
              class="button is-primary is-outlined is-fullwidth"
              @click="addSize"
              :disabled="isSubmitting"
            >
              <span class="icon">
                <i class="fas fa-plus"></i>
              </span>
              <span>Добавить размер</span>
            </button>
          </div>
        </form>
      </section>

      <footer class="modal-card-foot">
        <div class="buttons">
          <button
            type="button"
            class="button is-primary"
            :disabled="isSubmitting || !isFormValid"
            @click="submitForm"
          >
            <span class="icon" v-if="isSubmitting">
              <i class="fas fa-spinner fa-spin"></i>
            </span>
            <span>{{ isSubmitting ? 'Добавление...' : 'Добавить товар' }}</span>
          </button>
          <button
            type="button"
            class="button"
            @click="$emit('close')"
            :disabled="isSubmitting"
          >
            Отмена
          </button>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { shopAPI } from '@/api/shop.ts'
import AppNotification from '@/components/AppNotification.vue'

interface Props {
  isVisible: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'product-added'): void
}

interface NotificationState {
  visible: boolean
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const fileInput = ref<HTMLInputElement | null>(null)
const isSubmitting = ref(false)

const selectedImages = ref<File[]>([])
const notification = ref<NotificationState>({
  visible: false,
  message: '',
  type: 'info'
})

const formData = ref({
  name: '',
  description: '',
  category: '',
  basePrice: 0,
  popularity: 0,
  sizes: [
    {
      sizeName: '',
      price: 0,
      countItems: 0
    }
  ]
})

const isFormValid = computed(() => {
  // Ограничиваем длину полей перед проверкой
  const name = formData.value.name.trim().substring(0, 100)
  const description = formData.value.description.trim().substring(0, 500)

  return name !== '' &&
    description !== '' &&
    formData.value.category !== '' &&
    formData.value.basePrice > 0 &&
    formData.value.basePrice <= 1000000 &&
    formData.value.sizes.every(size => {
      const sizeName = size.sizeName.trim().substring(0, 50)
      return sizeName !== '' &&
        size.price >= 0 &&
        size.price <= 1000000 &&
        size.countItems >= 0 &&
        size.countItems <= 10000
    })
})

const getImageUrl = (image: File) => {
  return URL.createObjectURL(image)
}

const triggerFileInput = (event: Event) => {
  event.preventDefault()
  event.stopPropagation()

  if (!isSubmitting.value && fileInput.value) {
    fileInput.value.click()
  }
}

const handleImageSelect = (event: Event) => {
  const input = event.target as HTMLInputElement
  if (input.files && !isSubmitting.value) {
    const files = Array.from(input.files)

    const oversizedFiles = files.filter(file => file.size > 5 * 1024 * 1024)
    if (oversizedFiles.length > 0) {
      showNotification('Некоторые файлы превышают максимальный размер 5MB', 'warning')
      input.value = ''
      return
    }

    const invalidFiles = files.filter(file => !file.type.startsWith('image/'))
    if (invalidFiles.length > 0) {
      showNotification('Можно загружать только изображения (JPG, PNG, GIF)', 'warning')
      input.value = ''
      return
    }

    selectedImages.value.push(...files)
    input.value = ''
  }
}

const removeImage = (index: number) => {
  if (!isSubmitting.value) {
    const removedImage = selectedImages.value[index]
    selectedImages.value.splice(index, 1)
    URL.revokeObjectURL(URL.createObjectURL(removedImage))
  }
}

const addSize = () => {
  if (!isSubmitting.value) {
    formData.value.sizes.push({
      sizeName: '',
      price: 0,
      countItems: 0
    })
  }
}

const removeSize = (index: number) => {
  if (formData.value.sizes.length > 1 && !isSubmitting.value) {
    formData.value.sizes.splice(index, 1)
  }
}

const submitForm = async () => {
  if (isSubmitting.value || !isFormValid.value) return

  // Ограничиваем длину полей перед отправкой
  const productData = {
    name: formData.value.name.trim().substring(0, 100),
    description: formData.value.description.trim().substring(0, 500),
    category: formData.value.category,
    basePrice: Math.min(Math.max(0, formData.value.basePrice), 1000000),
    popularity: 0
  }

  const sizesData = formData.value.sizes
    .filter(size => {
      const sizeName = size.sizeName.trim().substring(0, 50)
      const price = Math.min(Math.max(0, size.price), 1000000)
      const countItems = Math.min(Math.max(0, size.countItems), 10000)
      return sizeName !== '' && price >= 0 && countItems >= 0
    })
    .map(size => ({
      sizeName: size.sizeName.trim().substring(0, 50),
      price: Math.min(Math.max(0, size.price), 1000000),
      countItems: Math.min(Math.max(0, size.countItems), 10000)
    }))

  // Проверяем, есть ли хотя бы один размер
  if (sizesData.length === 0) {
    showNotification('Добавьте хотя бы один размер товара', 'warning')
    return
  }

  isSubmitting.value = true

  try {
    const formDataToSend = new FormData()
    formDataToSend.append('product', JSON.stringify(productData))
    formDataToSend.append('sizes', JSON.stringify(sizesData))

    selectedImages.value.forEach((image, index) => {
      formDataToSend.append('images', image)
    })

    const response = await shopAPI.createProductWithImages(formDataToSend)
    const productId = response.data.id

    emit('product-added')
    emit('close')
    showNotification('Товар успешно добавлен!', 'success')
    resetForm()

  } catch (error: any) {
    console.error('Ошибка при добавлении товара:', error)

    let errorMessage = 'Не удалось добавить товар'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.status === 409) {
      errorMessage = 'Товар с таким названием уже существует'
    } else if (error.message) {
      errorMessage = error.message
    }

    showNotification(errorMessage, 'error')
  } finally {
    isSubmitting.value = false
  }
}

const resetForm = () => {
  formData.value = {
    name: '',
    description: '',
    category: '',
    basePrice: 0,
    popularity: 0,
    sizes: [
      {
        sizeName: '',
        price: 0,
        countItems: 0
      }
    ]
  }

  selectedImages.value.forEach(image => {
    URL.revokeObjectURL(URL.createObjectURL(image))
  })
  selectedImages.value = []
}

const showNotification = (message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') => {
  notification.value = {
    visible: true,
    message,
    type
  }

  // Автоматически скрыть уведомление через 5 секунд
  setTimeout(() => {
    notification.value.visible = false
  }, 5000)
}

const hideNotification = () => {
  notification.value.visible = false
}

watch(() => props.isVisible, (newValue) => {
  if (!newValue) {
    resetForm()
  }
})

defineExpose({
  resetForm
})
</script>

<style scoped>
.modal-card {
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-card-body {
  padding: 1.5rem;
}

.modal-card-foot {
  justify-content: flex-end;
  padding: 1rem 1.5rem;
}

.field:not(:last-child) {
  margin-bottom: 1.5rem;
}

.box {
  background-color: #232121;
  border: 1px solid #e5e7eb;
  padding: 1rem;
  border-radius: 8px;
}

.button.is-primary {
  background-color: #667eea;
  color: white;
  border: none;
}

.button.is-primary:hover {
  opacity: 0.9;
}

.button.is-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.button.is-primary.is-outlined {
  background: transparent;
  border: 1px solid #667eea;
  color: #667eea;
}

.button.is-primary.is-outlined:hover {
  background-color: #667eea;
  color: white;
}

.button.is-danger {
  background-color: #ef4444;
  color: white;
  border: none;
}

.button.is-danger:hover {
  background-color: #dc2626;
}

.button.is-small {
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
}

.image-upload-section {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  background: #252527;
  transition: border-color 0.3s;
}

.image-upload-section:hover {
  border-color: #3273dc;
}

.image-preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}

.image-preview-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ddd;
  transition: transform 0.2s;
}

.image-preview-item:hover {
  transform: scale(1.05);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.delete-image-button {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.9);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: background-color 0.2s;
}

.delete-image-button:hover:not(:disabled) {
  background: rgba(220, 38, 38, 1);
}

.delete-image-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.file.is-boxed {
  margin: 0 auto;
}

.file.is-boxed.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.file-label {
  cursor: pointer;
}

.file-label.is-disabled {
  cursor: not-allowed;
}

.file-input {
  display: none;
}

.file-cta {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 1em;
  transition: all 0.2s;
}

.file-cta:hover:not(.is-disabled) {
  background-color: #f0f0f0;
}

.input:disabled,
.textarea:disabled,
.select select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
  opacity: 0.7;
}

.columns {
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.75rem;
}

.column {
  padding: 0.75rem;
}

@media (max-width: 768px) {
  .modal-card {
    width: 95%;
    margin: 0.5rem;
  }

  .columns {
    flex-direction: column;
  }

  .image-preview-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }

  .image-preview-item {
    width: 80px;
    height: 80px;
  }
}
</style>
