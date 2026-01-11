<template>
  <div class="tournaments-view">
    <!-- Компонент уведомлений -->
    <AppNotification
      :notification="notification"
      @hide="hideNotification"
    />

    <div class="mb-5">
      <h1 class="title is-2">Соревнования по художественной гимнастике</h1>
      <p class="subtitle is-5 has-text-grey">
        Календарь турниров и соревнований
      </p>
    </div>

    <div class="columns">
      <div class="column is-three-quarters">
        <TournamentTable
          :tournaments="paginatedTournaments"
          :rangs="rangs"
          :loading="loading"
          :total-items="filteredAndSortedTournaments.length"
          :current-page="currentPage"
          @search="handleSearch"
          @filter-by-rang="handleFilterByRang"
          @sort="handleSort"
          @page-change="handlePageChange"
          @view="showTournamentDetail"
          @edit="editTournament"
          @delete="deleteTournament"
          @create="createTournament"
        />
      </div>

      <div class="column">
        <div class="box">
          <h3 class="title is-5 mb-3">Фильтры</h3>

          <div class="field">
            <label class="label">Показать:</label>
            <div class="control">
              <label class="radio mr-3">
                <input
                  type="radio"
                  v-model="showArchived"
                  :value="false"
                  @change="applyFilters"
                >
                Только активные
              </label>
              <label class="radio">
                <input
                  type="radio"
                  v-model="showArchived"
                  :value="true"
                  @change="applyFilters"
                >
                Все турниры
              </label>
            </div>
          </div>

          <hr class="my-4">

          <div class="field">
            <label class="label">Сортировка по дате:</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select v-model="dateFilter" @change="applyFilters">
                  <option value="all">Все даты</option>
                  <option value="upcoming">Предстоящие</option>
                  <option value="past">Прошедшие</option>
                  <option value="current">Текущие</option>
                </select>
              </div>
            </div>
          </div>

          <hr class="my-4">

          <div class="has-text-centered">
            <button
              class="button is-link is-light is-fullwidth"
              @click="resetFilters"
            >
              <span class="icon">
                <i class="fas fa-redo"></i>
              </span>
              <span>Сбросить фильтры</span>
            </button>
          </div>
        </div>

        <div class="box">
          <h3 class="title is-5 mb-3">Статистика</h3>
          <div class="content">
            <p>Всего турниров: <strong>{{ totalTournaments }}</strong></p>
            <p>Активных: <strong>{{ activeTournaments }}</strong></p>
            <p>Архивных: <strong>{{ archivedTournaments }}</strong></p>
          </div>
        </div>
      </div>
    </div>

    <div class="modal" :class="{ 'is-active': showFormModal }">
      <div class="modal-background" @click="closeFormModal"></div>
      <div class="modal-card">
        <header class="modal-card-head">
          <p class="modal-card-title">
            {{ editingTournament ? 'Редактирование турнира' : 'Создание нового турнира' }}
          </p>
          <button class="delete" @click="closeFormModal"></button>
        </header>

        <section class="modal-card-body">
          <TournamentForm
            v-if="showFormModal"
            :tournament="editingTournament"
            :rangs="rangs"
            @submit="handleFormSubmit"
            @cancel="closeFormModal"
          />
        </section>
      </div>
    </div>

    <TournamentDetailModal
      :tournament="selectedTournament"
      :rangs="rangs"
      :is-visible="showDetailModal"
      @close="closeDetailModal"
      @edit="editSelectedTournament"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import TournamentTable from '../components/tournaments/TournamentTable.vue'
import TournamentForm from '../components/tournaments/TournamentForm.vue'
import TournamentDetailModal from '../components/tournaments/TournamentDetailModal.vue'
import { tournamentsAPI } from '../api/tournaments'
import type { Tournament, Rang } from '../types/tournaments'
import AppNotification from '@/components/AppNotification.vue'

const allTournaments = ref<Tournament[]>([])
const rangs = ref<Rang[]>([])
const loading = ref(false)

const searchQuery = ref('')
const selectedRangId = ref<number | null>(null)
const sortField = ref('startDate')
const sortDirection = ref('desc')
const showArchived = ref(false)
const dateFilter = ref('all')
const currentPage = ref(1)
const itemsPerPage = 10

const showFormModal = ref(false)
const showDetailModal = ref(false)
const editingTournament = ref<Tournament | null>(null)
const selectedTournament = ref<Tournament | null>(null)

interface NotificationState {
  visible: boolean
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
}

const notification = ref<NotificationState>({
  visible: false,
  message: '',
  type: 'info'
})

const loadTournaments = async () => {
  loading.value = true
  try {
    const result = await tournamentsAPI.getTournaments()
    allTournaments.value = result.data
  } catch (error) {
    console.error('Ошибка при загрузке турниров:', error)
    showNotification('Не удалось загрузить турниры', 'error')
  } finally {
    loading.value = false
  }
}

const loadRangs = async () => {
  try {
    const response = await tournamentsAPI.getRangs()
    rangs.value = response.data
  } catch (error) {
    console.error('Ошибка при загрузке рангов:', error)
    showNotification('Ошибка при загрузке рангов', 'error')
  }
}

const filteredAndSortedTournaments = computed(() => {
  let filtered = [...allTournaments.value]

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(t =>
      t.name.toLowerCase().includes(query)
    )
  }

  if (selectedRangId.value !== null) {
    filtered = filtered.filter(t => t.rangId === selectedRangId.value)
  }

  if (!showArchived.value) {
    filtered = filtered.filter(t => !t.archived)
  }

  if (dateFilter.value !== 'all') {
    const now = new Date()
    filtered = filtered.filter(t => {
      const startDate = new Date(t.startDate)
      const finishDate = new Date(t.finishDate)

      switch (dateFilter.value) {
        case 'upcoming':
          return startDate > now
        case 'past':
          return finishDate < now
        case 'current':
          return startDate <= now && finishDate >= now
        default:
          return true
      }
    })
  }

  filtered.sort((a, b) => {
    let aValue: any, bValue: any

    switch (sortField.value) {
      case 'name':
        aValue = a.name.toLowerCase()
        bValue = b.name.toLowerCase()
        break
      case 'startDate':
        aValue = new Date(a.startDate).getTime()
        bValue = new Date(b.startDate).getTime()
        break
      case 'finishDate':
        aValue = new Date(a.finishDate).getTime()
        bValue = new Date(b.finishDate).getTime()
        break
      case 'rangId':
        aValue = a.rangId
        bValue = b.rangId
        break
      case 'minimalAge':
        aValue = a.minimalAge
        bValue = b.minimalAge
        break
      default:
        return 0
    }

    const direction = sortDirection.value === 'asc' ? 1 : -1

    if (aValue < bValue) return -1 * direction
    if (aValue > bValue) return 1 * direction
    return 0
  })


  return filtered
})


const paginatedTournaments = computed(() => {
  const startIndex = (currentPage.value - 1) * itemsPerPage
  const endIndex = startIndex + itemsPerPage
  return filteredAndSortedTournaments.value.slice(startIndex, endIndex)
})

const totalTournaments = computed(() => allTournaments.value.length)
const activeTournaments = computed(() => allTournaments.value.filter(t => !t.archived).length)
const archivedTournaments = computed(() => allTournaments.value.filter(t => t.archived).length)

const handleSearch = (query: string) => {
  searchQuery.value = query
  currentPage.value = 1
}

const handleFilterByRang = (rangId: number | null) => {
  selectedRangId.value = rangId
  currentPage.value = 1
}

const handleSort = (field: string, direction: string) => {
  sortField.value = field
  sortDirection.value = direction
  currentPage.value = 1
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}

const applyFilters = () => {
  currentPage.value = 1
}

const resetFilters = () => {
  searchQuery.value = ''
  selectedRangId.value = null
  sortField.value = 'startDate'
  sortDirection.value = 'desc'
  showArchived.value = false
  dateFilter.value = 'all'
  currentPage.value = 1
}

const showTournamentDetail = (tournament: Tournament) => {
  selectedTournament.value = tournament
  showDetailModal.value = true
}

const closeDetailModal = () => {
  selectedTournament.value = null
  showDetailModal.value = false
}

const createTournament = () => {
  editingTournament.value = null
  showFormModal.value = true
}

const editTournament = (tournament: Tournament) => {
  editingTournament.value = tournament
  showFormModal.value = true
}

const editSelectedTournament = (tournament: Tournament) => {
  closeDetailModal()
  editTournament(tournament)
}

const closeFormModal = () => {
  editingTournament.value = null
  showFormModal.value = false
}

const handleFormSubmit = async (tournamentData: any) => {
  try {
    if (editingTournament.value) {
      await tournamentsAPI.updateTournament(editingTournament.value.id, tournamentData)
      showNotification('Турнир успешно обновлен!', 'success')
    } else {
      await tournamentsAPI.createTournament(tournamentData)
      showNotification('Турнир успешно создан!', 'success')
    }

    closeFormModal()
    await loadTournaments()
  } catch (error: any) {
    const message = error.response?.data?.message || 'Ошибка при сохранении турнира'
    showNotification(message, 'error')
  }
}

const deleteTournament = async (tournament: Tournament) => {
  try {
    await tournamentsAPI.deleteTournament(tournament.id)
    await loadTournaments()
    showNotification('Турнир успешно удален!', 'success')
  } catch (error: any) {
    const message = error.response?.data?.message || 'Ошибка при удалении турнира'
    showNotification(message, 'error')
  }
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

onMounted(() => {
  loadTournaments()
  loadRangs()
})
</script>

<style scoped>
.tournaments-view {
  padding: 1rem;
}

.title {
  color: #afd5a5;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #aad6ac;
}

.box {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

.box h3 {
  color: #8fb891;
}

.radio {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.radio input {
  margin-right: 0.5rem;
}

.modal-card {
  width: 90%;
  max-width: 1200px;
  border-radius: 12px;
  overflow: hidden;
  margin: 0 auto;
}

.modal-card-head {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: none;
  padding: 1.5rem;
}

.modal-card-title {
  color: white;
  font-size: 1.5rem;
  font-weight: 600;
}

@media (max-width: 768px) {
  .columns {
    flex-direction: column;
  }

  .column.is-three-quarters {
    width: 100%;
  }

  .modal-card {
    width: 95%;
    max-width: 95%;
  }
}
</style>
