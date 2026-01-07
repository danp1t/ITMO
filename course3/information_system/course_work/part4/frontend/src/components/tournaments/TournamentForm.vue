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
                maxlength="100"
                required
              >
            </div>
            <p v-if="errors.name" class="help is-danger">{{ errors.name }}</p>
            <p class="help has-text-grey-light">{{ form.name.length }}/100 символов</p>
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
                    :max="form.finishDate || maxDate"
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
                    :min="form.startDate || minDate"
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
                maxlength="200"
                required
              ></textarea>
            </div>
            <p v-if="errors.address" class="help is-danger">{{ errors.address }}</p>
            <p class="help has-text-grey-light">{{ form.address.length }}/200 символов</p>
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
                maxlength="500"
              >
            </div>
            <p v-if="errors.link" class="help is-danger">{{ errors.link }}</p>
            <p v-if="form.link" class="help has-text-grey-light">{{ form.link.length }}/500 символов</p>
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

      <!-- Уведомление -->
      <AppNotification
        :notification="notification"
        @hide="hideNotification"
      />

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
import type { Tournament, Rang } from '@/types/tournaments.ts'
import AppNotification from '@/components/AppNotification.vue'

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

// Ограничения по датам
const currentDate = new Date()
const maxDate = new Date(currentDate.getFullYear() + 5, 11, 31).toISOString().slice(0, 16)
const minDate = new Date(2000, 0, 1).toISOString().slice(0, 16)

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

const notification = ref({
  visible: false,
  message: '',
  type: 'info' as 'info' | 'success' | 'warning' | 'error'
})

const isLoading = ref(false)

const isEditMode = computed(() => !!props.tournament)

const showNotification = (message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info', duration: number = 5000) => {
  notification.value = {
    visible: true,
    message,
    type
  }

  if (duration > 0) {
    setTimeout(() => {
      hideNotification()
    }, duration)
  }
}

const hideNotification = () => {
  notification.value.visible = false
}

const formatDateTimeLocal = (dateString: string) => {
  const date = new Date(dateString)
  return date.toISOString().slice(0, 16)
}

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

const validateForm = () => {
  let isValid = true

  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = ''
  })

  if (!form.name.trim()) {
    errors.name = 'Название обязательно'
    isValid = false
  } else if (form.name.trim().length > 100) {
    errors.name = 'Название не должно превышать 100 символов'
    isValid = false
  }

  if (!form.startDate) {
    errors.startDate = 'Дата начала обязательна'
    isValid = false
  } else {
    const start = new Date(form.startDate)
    const now = new Date()

    // Проверка на будущую дату (можно редактировать архивные)
    if (!form.archived && start < now) {
      errors.startDate = 'Дата начала не может быть в прошлом для активного турнира'
      isValid = false
    }
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

  if (!form.address.trim()) {
    errors.address = 'Адрес обязателен'
    isValid = false
  } else if (form.address.trim().length > 200) {
    errors.address = 'Адрес не должен превышать 200 символов'
    isValid = false
  }

  if (!form.rangId) {
    errors.rangId = 'Ранг обязателен'
    isValid = false
  }

  const age = parseInt(form.minimalAge)
  if (isNaN(age) || age < 0 || age > 100) {
    errors.minimalAge = 'Возраст должен быть от 0 до 100 лет'
    isValid = false
  }

  if (form.link && !isValidUrl(form.link)) {
    errors.link = 'Неверный формат URL'
    isValid = false
  } else if (form.link && form.link.length > 500) {
    errors.link = 'Ссылка не должна превышать 500 символов'
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
    showNotification('Пожалуйста, исправьте ошибки в форме', 'warning')
    return
  }

  isLoading.value = true

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
  } catch (error: any) {
    console.error('Ошибка при сохранении данных:', error)

    let errorMessage = 'Ошибка при сохранении данных'
    if (error.response?.data) {
      errorMessage = `Ошибка: ${error.response.data}`
    } else if (error.response?.status === 400) {
      errorMessage = 'Некорректные данные. Проверьте введённые значения'
    } else if (error.response?.status === 401) {
      errorMessage = 'Недостаточно прав для выполнения операции'
    }

    showNotification(errorMessage, 'error', 7000)
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.tournament-form {
  background: #333131;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.select {
  height: 3.5rem; /* Увеличиваем высоту */
  font-size: 1.1rem; /* Увеличиваем размер шрифта */
  line-height: 1.5; /* Увеличиваем межстрочный интервал */
}

.label {
  font-weight: 600;
  color: #eddddd;
  margin-bottom: 0.5rem;
}

.input, .textarea, .select select {
  border: 2px solid #010c7a;
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

.help.has-text-grey-light {
  color: #b5b5b5;
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

.checkbox {
  display: flex;
  align-items: center;
}

.checkbox input {
  margin-right: 0.5rem;
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
