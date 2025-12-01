<template>
  <div class="tournament-form">
    <form @submit.prevent="handleSubmit">
      <div class="columns">
        <div class="column">
          <!-- Название -->
          <div class="field">
            <label class="label">Название турнира *</label>
            <div class="control">
              <input
                v-model="form.name"
                type="text"
                class="input"
                :class="{ 'is-danger': errors.name }"
                placeholder="Введите название турнира"
                required
              >
            </div>
            <p v-if="errors.name" class="help is-danger">{{ errors.name }}</p>
          </div>

          <!-- Даты -->
          <div class="columns">
            <div class="column">
              <div class="field">
                <label class="label">Дата начала *</label>
                <div class="control">
                  <input
                    v-model="form.startDate"
                    type="datetime-local"
                    class="input"
                    :class="{ 'is-danger': errors.startDate }"
                    required
                  >
                </div>
                <p v-if="errors.startDate" class="help is-danger">{{ errors.startDate }}</p>
              </div>
            </div>

            <div class="column">
              <div class="field">
                <label class="label">Дата окончания *</label>
                <div class="control">
                  <input
                    v-model="form.finishDate"
                    type="datetime-local"
                    class="input"
                    :class="{ 'is-danger': errors.finishDate }"
                    required
                  >
                </div>
                <p v-if="errors.finishDate" class="help is-danger">{{ errors.finishDate }}</p>
              </div>
            </div>
          </div>

          <!-- Адрес -->
          <div class="field">
            <label class="label">Адрес проведения *</label>
            <div class="control">
              <textarea
                v-model="form.address"
                class="textarea"
                :class="{ 'is-danger': errors.address }"
                placeholder="Введите адрес проведения турнира"
                rows="3"
                required
              ></textarea>
            </div>
            <p v-if="errors.address" class="help is-danger">{{ errors.address }}</p>
          </div>

          <!-- Ссылка -->
          <div class="field">
            <label class="label">Ссылка на турнир</label>
            <div class="control">
              <input
                v-model="form.link"
                type="url"
                class="input"
                :class="{ 'is-danger': errors.link }"
                placeholder="https://example.com/tournament"
              >
            </div>
            <p v-if="errors.link" class="help is-danger">{{ errors.link }}</p>
          </div>
        </div>

        <div class="column">
          <!-- Ранг -->
          <div class="field">
            <label class="label">Ранг турнира *</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select
                  v-model="form.rangId"
                  class="select"
                  :class="{ 'is-danger': errors.rangId }"
                  required
                >
                  <option value="" disabled>Выберите ранг</option>
                  <option
                    v-for="rang in rangs"
                    :key="rang.id"
                    :value="rang.id"
                  >
                    {{ rang.name }}
                  </option>
                </select>
              </div>
            </div>
            <p v-if="errors.rangId" class="help is-danger">{{ errors.rangId }}</p>
          </div>

          <!-- Минимальный возраст -->
          <div class="field">
            <label class="label">Минимальный возраст *</label>
            <div class="control">
              <input
                v-model="form.minimalAge"
                type="number"
                min="0"
                max="100"
                class="input"
                :class="{ 'is-danger': errors.minimalAge }"
                placeholder="0"
                required
              >
            </div>
            <p v-if="errors.minimalAge" class="help is-danger">{{ errors.minimalAge }}</p>
          </div>

          <!-- Статус архивности -->
          <div class="field">
            <div class="control">
              <label class="checkbox">
                <input
                  v-model="form.archived"
                  type="checkbox"
                >
                <span class="ml-2">Архивный турнир</span>
              </label>
            </div>
            <p class="help">Архивные турниры отображаются отдельно</p>
          </div>
        </div>
      </div>

      <!-- Ошибки -->
      <div v-if="serverError" class="notification is-danger is-light mb-4">
        {{ serverError }}
      </div>

      <!-- Кнопки -->
      <div class="field is-grouped is-grouped-right">
        <div class="control">
          <button
            type="button"
            class="button is-light"
            @click="$emit('cancel')"
            :disabled="isLoading"
          >
            Отмена
          </button>
        </div>
        <div class="control">
          <button
            type="submit"
            class="button is-primary"
            :class="{ 'is-loading': isLoading }"
            :disabled="isLoading"
          >
            {{ isEditMode ? 'Обновить' : 'Создать' }}
          </button>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import type { Tournament, Rang } from '../../types/tournaments'

interface Props {
  tournament?: Tournament
  rangs: Rang[]
}

interface Emits {
  (e: 'submit', data: any): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const form = reactive({
  name: '',
  startDate: '',
  finishDate: '',
  address: '',
  link: '',
  rangId: '',
  minimalAge: '',
  archived: false
})

const errors = reactive({
  name: '',
  startDate: '',
  finishDate: '',
  address: '',
  link: '',
  rangId: '',
  minimalAge: ''
})

const serverError = ref('')
const isLoading = ref(false)

const isEditMode = computed(() => !!props.tournament)

// Заполняем форму при редактировании
watch(() => props.tournament, (tournament) => {
  if (tournament) {
    form.name = tournament.name
    form.startDate = formatDateTimeLocal(tournament.startDate)
    form.finishDate = formatDateTimeLocal(tournament.finishDate)
    form.address = tournament.address
    form.link = tournament.link || ''
    form.rangId = tournament.rangId.toString()
    form.minimalAge = tournament.minimalAge.toString()
    form.archived = tournament.archived
  }
}, { immediate: true })

const formatDateTimeLocal = (dateString: string) => {
  const date = new Date(dateString)
  return date.toISOString().slice(0, 16)
}

const validateForm = () => {
  let isValid = true

  // Сброс ошибок
  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = ''
  })

  // Проверка названия
  if (!form.name.trim()) {
    errors.name = 'Название обязательно'
    isValid = false
  }

  // Проверка дат
  if (!form.startDate) {
    errors.startDate = 'Дата начала обязательна'
    isValid = false
  }

  if (!form.finishDate) {
    errors.finishDate = 'Дата окончания обязательна'
    isValid = false
  }

  if (form.startDate && form.finishDate) {
    const start = new Date(form.startDate)
    const finish = new Date(form.finishDate)

    if (start >= finish) {
      errors.finishDate = 'Дата окончания должна быть позже даты начала'
      isValid = false
    }
  }

  // Проверка адреса
  if (!form.address.trim()) {
    errors.address = 'Адрес обязателен'
    isValid = false
  }

  // Проверка ранга
  if (!form.rangId) {
    errors.rangId = 'Ранг обязателен'
    isValid = false
  }

  // Проверка возраста
  const age = parseInt(form.minimalAge)
  if (isNaN(age) || age < 0 || age > 100) {
    errors.minimalAge = 'Возраст должен быть от 0 до 100 лет'
    isValid = false
  }

  // Проверка ссылки
  if (form.link && !isValidUrl(form.link)) {
    errors.link = 'Неверный формат URL'
    isValid = false
  }

  return isValid
}

const isValidUrl = (url: string) => {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  isLoading.value = true
  serverError.value = ''

  try {
    const tournamentData = {
      name: form.name.trim(),
      startDate: new Date(form.startDate).toISOString(),
      finishDate: new Date(form.finishDate).toISOString(),
      address: form.address.trim(),
      link: form.link.trim() || undefined,
      rangId: parseInt(form.rangId),
      minimalAge: parseInt(form.minimalAge),
      archived: form.archived
    }

    emit('submit', tournamentData)
  } catch (error) {
    serverError.value = 'Ошибка при сохранении данных'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.tournament-form {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.label {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.input, .textarea, .select select {
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  font-size: 1rem;
  transition: all 0.3s;
}

.input:focus, .textarea:focus, .select select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  outline: none;
}

.input.is-danger, .textarea.is-danger, .select.is-danger select {
  border-color: #ff3860;
}

.help.is-danger {
  color: #ff3860;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.checkbox {
  display: flex;
  align-items: center;
}

.checkbox input {
  margin-right: 0.5rem;
}

.notification {
  border-radius: 8px;
  border: none;
}

@media (max-width: 768px) {
  .tournament-form {
    padding: 1.5rem;
  }

  .columns {
    flex-direction: column;
  }
}
</style>
