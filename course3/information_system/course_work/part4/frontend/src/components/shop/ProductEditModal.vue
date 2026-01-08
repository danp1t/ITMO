<template>
  <div class="modal" :class="{ 'is-active': isVisible }">
    <div class="modal-background" @click="closeModal"></div>

    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">Редактировать товар</p>
        <button class="delete" aria-label="close" @click="closeModal"></button>
      </header>

      <section class="modal-card-body">
        <form @submit.prevent="submitForm">
          <!-- Основная информация -->
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
              >
            </div>
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
              ></textarea>
            </div>
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
                      <option value="Балетки">Балетки</option>
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
                    required
                    placeholder="0"
                    :disabled="isSubmitting"
                  >
                </div>
              </div>
            </div>
          </div>

          <!-- Существующие изображения -->
          <div class="field" v-if="existingImages.length > 0">
            <label class="label">Текущие изображения</label>
            <div class="existing-images">
              <div class="image-preview-grid">
                <div
                  v-for="(image, index) in existingImages"
                  :key="index"
                  class="image-preview-item"
                >
                  <img :src="getImageUrl(image)" :alt="`Изображение ${index + 1}`" class="preview-image">
                  <button
                    type="button"
                    class="delete-image-button"
                    @click="markImageForDeletion(index)"
                    :disabled="isSubmitting"
                  >
                    <i class="fas fa-times"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Новые изображения -->
          <div class="field">
            <label class="label">Добавить изображения</label>
            <div class="image-upload-section">
              <div v-if="newImages.length > 0" class="image-preview-grid">
                <div
                  v-for="(image, index) in newImages"
                  :key="`new-${index}`"
                  class="image-preview-item"
                >
                  <img :src="getNewImageUrl(image)" :alt="`Новое изображение ${index + 1}`" class="preview-image">
                  <button
                    type="button"
                    class="delete-image-button"
                    @click="removeNewImage(index)"
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
                    <span class="file-label">Добавить изображения</span>
                  </span>
                </label>
              </div>
            </div>
          </div>

          <!-- Размеры и цены -->
          <div class="field">
            <label class="label">Размеры и цены</label>

            <div
              v-for="(size, index) in formData.productInfos"
              :key="size.id || `new-${index}`"
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
                    :disabled="formData.productInfos.length <= 1 || isSubmitting"
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
                      >
                    </div>
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
                        required
                        placeholder="0"
                        :disabled="isSubmitting"
                      >
                    </div>
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
                        required
                        placeholder="0"
                        :disabled="isSubmitting"
                      >
                    </div>
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
            <span>{{ isSubmitting ? 'Сохранение...' : 'Сохранить изменения' }}</span>
          </button>
          <button
            type="button"
            class="button is-danger is-outlined"
            @click="deleteProduct"
            :disabled="isSubmitting"
          >
            Удалить товар
          </button>
          <button
            type="button"
            class="button"
            @click="closeModal"
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
import { ref, computed, onMounted, watch } from 'vue'
import { shopAPI } from '../../api/shop'
import type { Product, ProductDetail } from '../../types/shop'

interface Props {
  isVisible: boolean
  productId: number
}

interface Emits {
  (e: 'close'): void
  (e: 'product-updated'): void
  (e: 'product-deleted', productId: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const fileInput = ref<HTMLInputElement | null>(null)
const isSubmitting = ref(false)

// Данные формы
const formData = ref({
  id: 0,
  name: '',
  description: '',
  category: '',
  basePrice: 0,
  images: [] as string[],
  productInfos: [] as Array<{
    id?: number
    sizeName: string
    price: number
    countItems: number
  }>
})

// Изображения
const existingImages = ref<string[]>([])
const newImages = ref<File[]>([])
const imagesToDelete = ref<string[]>([])

// Валидация формы
const isFormValid = computed(() => {
  return formData.value.name.trim() !== '' &&
    formData.value.description.trim() !== '' &&
    formData.value.category !== '' &&
    formData.value.basePrice > 0 &&
    formData.value.productInfos.every(size =>
      size.sizeName.trim() !== '' &&
      size.price >= 0 &&
      size.countItems >= 0
    )
})

// Получение URL для изображения
const getImageUrl = (imagePath: string) => {
  if (imagePath.startsWith('http') || imagePath.startsWith('/')) {
    return imagePath
  }
  return `/api/products/images/${imagePath}`
}

const getNewImageUrl = (file: File) => {
  return URL.createObjectURL(file)
}

// Закрытие модального окна
const closeModal = () => {
  if (!isSubmitting.value) {
    resetForm()
    emit('close')
  }
}

// Загрузка данных товара
const loadProduct = async () => {
  try {
    const response = await shopAPI.getProductForEdit(props.productId)
    const product = response.data

    formData.value = {
      id: product.id,
      name: product.name,
      description: product.description,
      category: product.category,
      basePrice: product.basePrice,
      images: product.images || [],
      productInfos: product.productInfos || []
    }

    existingImages.value = product.images || []
  } catch (error) {
    console.error('Ошибка при загрузке товара:', error)
    alert('Не удалось загрузить данные товара')
    closeModal()
  }
}

// Управление изображениями
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

    // Проверка размера файлов (5MB)
    const oversizedFiles = files.filter(file => file.size > 5 * 1024 * 1024)
    if (oversizedFiles.length > 0) {
      alert('Некоторые файлы превышают максимальный размер 5MB')
      input.value = ''
      return
    }

    // Проверка типа файлов
    const invalidFiles = files.filter(file => !file.type.startsWith('image/'))
    if (invalidFiles.length > 0) {
      alert('Можно загружать только изображения (JPG, PNG, GIF)')
      input.value = ''
      return
    }

    newImages.value.push(...files)
    input.value = ''
  }
}

const markImageForDeletion = (index: number) => {
  const image = existingImages.value[index]
  if (!isSubmitting.value && confirm('Удалить это изображение?')) {
    imagesToDelete.value.push(image)
    existingImages.value.splice(index, 1)
  }
}

const removeNewImage = (index: number) => {
  if (!isSubmitting.value) {
    const removedImage = newImages.value[index]
    newImages.value.splice(index, 1)
    URL.revokeObjectURL(getNewImageUrl(removedImage))
  }
}

// Управление размерами
const addSize = () => {
  if (!isSubmitting.value) {
    formData.value.productInfos.push({
      sizeName: '',
      price: 0,
      countItems: 0
    })
  }
}

const removeSize = (index: number) => {
  if (formData.value.productInfos.length > 1 && !isSubmitting.value) {
    formData.value.productInfos.splice(index, 1)
  }
}

// Отправка формы (раздельно - сначала основные данные, затем изображения)
const submitForm = async () => {
  if (isSubmitting.value || !isFormValid.value) return

  isSubmitting.value = true

  try {
    // 1. Обновляем основные данные товара
    const productData = {
      name: formData.value.name.trim(),
      description: formData.value.description.trim(),
      category: formData.value.category,
      basePrice: formData.value.basePrice,
      images: existingImages.value // Текущие изображения
    }

    await shopAPI.updateProduct(props.productId, productData)

    // 2. Удаляем отмеченные изображения
    for (const imagePath of imagesToDelete.value) {
      try {
        await shopAPI.deleteProductImage(props.productId, imagePath)
      } catch (error) {
        console.warn('Не удалось удалить изображение:', imagePath, error)
      }
    }

    // 3. Добавляем новые изображения
    for (const imageFile of newImages.value) {
      try {
        const imageFormData = new FormData()
        imageFormData.append('image', imageFile)
        await shopAPI.uploadProductImage(props.productId, imageFormData)
      } catch (error) {
        console.warn('Не удалось загрузить изображение:', imageFile.name, error)
      }
    }

    // 4. Обновляем информацию о размерах (если нужно)
    // Для этого нужно создать/обновить ProductInfo
    for (const size of formData.value.productInfos) {
      try {
        if (size.id) {
          // Обновить существующую запись
          await shopAPI.updateProductInfo(size.id, {
            sizeName: size.sizeName,
            price: size.price,
            countItems: size.countItems
          })
        } else {
          // Создать новую запись
          await shopAPI.createProductInfo({
            productId: props.productId,
            sizeName: size.sizeName,
            price: size.price,
            countItems: size.countItems
          })
        }
      } catch (error) {
        console.warn('Не удалось обновить размер:', size, error)
      }
    }

    emit('product-updated')
    closeModal()

  } catch (error: any) {
    console.error('Ошибка при обновлении товара:', error)
    let errorMessage = 'Не удалось обновить товар'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.status === 404) {
      errorMessage = 'Товар не найден'
    }
    alert(errorMessage)
  } finally {
    isSubmitting.value = false
  }
}

// Удаление товара
const deleteProduct = async () => {
  if (isSubmitting.value) return

  isSubmitting.value = true

  try {
    await shopAPI.deleteProduct(props.productId)
    emit('product-deleted', props.productId)
    closeModal()
  } catch (error: any) {
    console.error('Ошибка при удалении товара:', error)
    let errorMessage = 'Не удалось удалить товар'
    if (error.response?.status === 404) {
      errorMessage = 'Товар не найден'
    }
    alert(errorMessage)
  } finally {
    isSubmitting.value = false
  }
}

// Сброс формы
const resetForm = () => {
  formData.value = {
    id: 0,
    name: '',
    description: '',
    category: '',
    basePrice: 0,
    images: [],
    productInfos: []
  }

  // Освобождаем URL объектов
  newImages.value.forEach(image => {
    URL.revokeObjectURL(getNewImageUrl(image))
  })
  newImages.value = []
  imagesToDelete.value = []
  existingImages.value = []
}

// Загрузка данных при изменении productId
watch(() => props.productId, (newId) => {
  if (newId) {
    loadProduct()
  }
}, { immediate: true })

// Загрузка данных при открытии модального окна
watch(() => props.isVisible, (newValue) => {
  if (newValue && props.productId) {
    loadProduct()
  }
})
</script>

<style scoped>
.modal-card {
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-card-foot .buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.button.is-danger.is-outlined {
  border-color: #ef4444;
  color: #ef4444;
}

.button.is-danger.is-outlined:hover {
  background-color: #ef4444;
  color: white;
}

.image-preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}

.image-preview-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ddd;
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
}

.delete-image-button:hover:not(:disabled) {
  background: rgba(220, 38, 38, 1);
}

.delete-image-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
