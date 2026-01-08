<template>
  <div class="modal" :class="{ 'is-active': isVisible }">
    <div class="modal-background" @click="$emit('close')"></div>

    <div class="modal-card">
      <header class="modal-card-head">
        <p class="modal-card-title">Добавить товар</p>
        <button class="delete" aria-label="close" @click="$emit('close')"></button>
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
              ></textarea>
            </div>
          </div>

          <div class="columns">
            <div class="column">
              <div class="field">
                <label class="label">Категория *</label>
                <div class="control">
                  <div class="select is-fullwidth">
                    <select v-model="formData.category" required>
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
                  >
                </div>
              </div>
            </div>
          </div>

          <!-- Изображения -->
          <div class="field">
            <label class="label">URL изображений (по одному на строку)</label>
            <div class="control">
              <textarea
                v-model="formData.imagesInput"
                class="textarea"
                placeholder="https://example.com/image1.jpg&#10;https://example.com/image2.jpg"
                rows="3"
              ></textarea>
            </div>
            <p class="help">Оставьте пустым для использования случайных изображений</p>
          </div>

          <!-- Размеры и цены -->
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
            :disabled="isSubmitting"
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
import { ref } from 'vue'
import { shopAPI } from '../../api/shop'
import type { CreateProductData, CreateProductInfoData } from '../../types/shop'

interface Props {
  isVisible: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'product-added'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const isSubmitting = ref(false)

// Данные формы
const formData = ref({
  name: '',
  description: '',
  category: '',
  basePrice: 0,
  popularity: 0,
  imagesInput: '',
  sizes: [
    {
      sizeName: '',
      price: 0,
      countItems: 0
    }
  ]
})

// Добавить размер
const addSize = () => {
  formData.value.sizes.push({
    sizeName: '',
    price: 0,
    countItems: 0
  })
}

// Удалить размер
const removeSize = (index: number) => {
  if (formData.value.sizes.length > 1) {
    formData.value.sizes.splice(index, 1)
  }
}

// Отправить форму
const submitForm = async () => {
  isSubmitting.value = true

  try {
    // Преобразуем изображения из текста в массив
    const images = formData.value.imagesInput
      .split('\n')
      .map(url => url.trim())
      .filter(url => url !== '')

    // Подготовка данных для создания товара
    const productData: CreateProductData = {
      name: formData.value.name,
      description: formData.value.description,
      category: formData.value.category,
      basePrice: formData.value.basePrice,
      popularity: 0,
      images: images.length > 0 ? images : undefined
    }

    // Создаем товар
    const productResponse = await shopAPI.createProduct(productData)
    const productId = productResponse.data.id

    // Создаем информацию о размерах
    for (const size of formData.value.sizes) {
      if (size.sizeName && size.price >= 0 && size.countItems >= 0) {
        const productInfoData: CreateProductInfoData = {
          productId,
          sizeName: size.sizeName,
          price: size.price,
          countItems: size.countItems
        }

        await shopAPI.createProductInfo(productInfoData)
      }
    }

    // Оповещаем об успешном добавлении
    emit('product-added')
    emit('close')

    // Сбрасываем форму
    resetForm()

  } catch (error: any) {
    console.error('Ошибка при добавлении товара:', error)

    let errorMessage = 'Не удалось добавить товар'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    }

    alert(errorMessage)
  } finally {
    isSubmitting.value = false
  }
}

// Сброс формы
const resetForm = () => {
  formData.value = {
    name: '',
    description: '',
    category: '',
    basePrice: 0,
    popularity: 0,
    imagesInput: '',
    sizes: [
      {
        sizeName: '',
        price: 0,
        countItems: 0
      }
    ]
  }
}
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
}



.button.is-primary:hover {
  opacity: 0.9;
}

.button.is-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
