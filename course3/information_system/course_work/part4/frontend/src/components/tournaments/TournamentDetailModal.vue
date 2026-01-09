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
        <div class="buttons are-medium">
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
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth.ts'
import type { Tournament, Rang } from '@/types/tournaments.ts'

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

<style scoped>
.modal-card-foot {
  display: flex;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid #dbdbdb;
}

.buttons {
  gap: 1rem;
  width: 100%;
  justify-content: flex-end;
}

.buttons .button {
  min-width: 120px;
  padding-left: 1.5rem;
  padding-right: 1.5rem;
}

.modal-card-foot .button {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.modal-card-foot .button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.modal-card-foot .button:first-child {
  background-color: #f5f5f5;
  border-color: #dbdbdb;
  color: #363636;
}

.modal-card-foot .button:first-child:hover {
  background-color: #e8e8e8;
  border-color: #c5c5c5;
}

.modal-card-foot .button.is-primary {
  background-color: #3273dc;
  border-color: #3273dc;
  color: white;
}

.modal-card-foot .button.is-primary:hover {
  background-color: #276cda;
  border-color: #276cda;
}

@media (max-width: 768px) {
  .modal-card-foot {
    padding: 1rem;
    flex-direction: column;
  }

  .buttons {
    flex-direction: column;
    gap: 0.75rem;
    width: 100%;
  }

  .buttons .button {
    width: 100%;
    justify-content: center;
  }
}
</style>
