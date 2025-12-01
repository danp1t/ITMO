<template>
  <div class="modal" :class="{ 'is-active': isVisible && tournament }">
    <div class="modal-background" @click="$emit('close')"></div>
    <div class="modal-card" v-if="tournament">
      <header class="modal-card-head">
        <p class="modal-card-title">{{ tournament.name }}</p>
        <button class="delete" @click="$emit('close')"></button>
      </header>

      <section class="modal-card-body">
        <div class="content">
          <div class="columns">
            <div class="column">
              <div class="field">
                <label class="label has-text-weight-semibold">Даты проведения:</label>
                <p>
                  с {{ formatDate(tournament.startDate) }}
                  по {{ formatDate(tournament.finishDate) }}
                </p>
              </div>

              <div class="field">
                <label class="label has-text-weight-semibold">Адрес:</label>
                <p>{{ tournament.address }}</p>
              </div>

              <div v-if="tournament.link" class="field">
                <label class="label has-text-weight-semibold">Ссылка:</label>
                <p>
                  <a :href="tournament.link" target="_blank" class="has-text-primary">
                    {{ tournament.link }}
                    <span class="icon is-small ml-1">
                      <i class="fas fa-external-link-alt"></i>
                    </span>
                  </a>
                </p>
              </div>
            </div>

            <div class="column">
              <div class="field">
                <label class="label has-text-weight-semibold">Ранг:</label>
                <span class="tag" :class="getRangColor(tournament.rangId)">
                  {{ getRangName(tournament.rangId) }}
                </span>
              </div>

              <div class="field">
                <label class="label has-text-weight-semibold">Минимальный возраст:</label>
                <p>{{ tournament.minimalAge }} лет</p>
              </div>

              <div class="field">
                <label class="label has-text-weight-semibold">Статус:</label>
                <span
                  class="tag"
                  :class="tournament.archived ? 'is-dark' : 'is-success'"
                >
                  {{ tournament.archived ? 'Архивный' : 'Активный' }}
                </span>
              </div>
            </div>
          </div>

          <div v-if="rangDescription" class="field mt-4">
            <label class="label has-text-weight-semibold">Описание ранга:</label>
            <p class="has-text-grey">{{ rangDescription }}</p>
          </div>
        </div>
      </section>

      <footer class="modal-card-foot">
        <button class="button" @click="$emit('close')">
          Закрыть
        </button>
        <button
          v-if="showEditButton"
          class="button is-primary"
          @click="$emit('edit', tournament)"
        >
          <span class="icon">
            <i class="fas fa-edit"></i>
          </span>
          <span>Редактировать</span>
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import type { Tournament, Rang } from '../../types/tournaments'

interface Props {
  tournament: Tournament | null
  rangs: Rang[]
  isVisible: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'edit', tournament: Tournament): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const authStore = useAuthStore()

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  })
}

const getRangName = (rangId: number) => {
  const rang = props.rangs.find(r => r.id === rangId)
  return rang ? rang.name : 'Неизвестно'
}

const getRangColor = (rangId: number) => {
  const colors = ['is-primary', 'is-success', 'is-info', 'is-warning', 'is-danger']
  return colors[rangId % colors.length]
}

const rangDescription = computed(() => {
  if (!props.tournament) return ''
  const rang = props.rangs.find(r => r.id === props.tournament!.rangId)
  return rang ? rang.description : ''
})

const showEditButton = computed(() => {
  if (!props.tournament || !authStore.user) return false
  return authStore.user.roles.includes('OAPI:ROLE:EditTournament')
})
</script>
