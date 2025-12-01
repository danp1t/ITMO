<template>
  <div class="tournaments-view">
    <!-- Заголовок -->
    <div class="mb-5">
      <h1 class="title is-2">Соревнования по художественной гимнастике</h1>
      <p class="subtitle is-5 has-text-grey">
        Календарь турниров и соревнований
      </p>
    </div>

    <!-- Основной контент -->
    <div class="columns">
      <!-- Таблица турниров -->
      <div class="column is-three-quarters">
        <TournamentTable
          :tournaments="tournaments"
          :rangs="rangs"
          :loading="loading"
          :total-items="totalTournaments"
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

      <!-- Боковая панель -->
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
                  @change="loadTournaments"
                >
                Только активные
              </label>
              <label class="radio">
                <input
                  type="radio"
                  v-model="showArchived"
                  :value="true"
                  @change="loadTournaments"
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
                <select v-model="dateFilter" @change="loadTournaments">
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

        <!-- Статистика -->
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

    <!-- Модальное окно создания/редактирования -->
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

    <!-- Модальное окно деталей -->
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
import { useAuthStore } from '../stores/auth'
import TournamentTable from '../components/tournaments/TournamentTable.vue'
import TournamentForm from '../components/tournaments/TournamentForm.vue'
import TournamentDetailModal from '../components/tournaments/TournamentDetailModal.vue'
import { tournamentsAPI } from '../api/tournaments'
import type { Tournament, Rang } from '../types/tournaments'

const authStore = useAuthStore()

// Данные
const tournaments = ref<Tournament[]>([])
const rangs = ref<Rang[]>([])
const loading = ref(false)
const totalTournaments = ref(0)

// Фильтры
const searchQuery = ref('')
const selectedRangId = ref<number | null>(null)
const sortField = ref('startDate')
const sortDirection = ref('desc')
const showArchived = ref(false)
const dateFilter = ref('all')
const currentPage = ref(1)
const itemsPerPage = 10

// Модальные окна
const showFormModal = ref(false)
const showDetailModal = ref(false)
const editingTournament = ref<Tournament | null>(null)
const selectedTournament = ref<Tournament | null>(null)

const activeTournaments = computed(() => {
  return tournaments.value.filter(t => !t.archived).length
})

const archivedTournaments = computed(() => {
  return tournaments.value.filter(t => t.archived).length
})

const loadTournaments = async () => {
  loading.value = true

  try {
    let params: any = {
      sortBy: sortField.value,
      sortDirection: sortDirection.value,
      page: currentPage.value,
      limit: itemsPerPage
    }

    // Применяем фильтры
    if (searchQuery.value) {
      const searchResult = await tournamentsAPI.searchTournaments(
        searchQuery.value,
        sortField.value,
        sortDirection.value
      )
      tournaments.value = searchResult.data
      totalTournaments.value = searchResult.data.length
    } else if (selectedRangId.value) {
      const rangResult = await tournamentsAPI.getTournamentsByRang(selectedRangId.value)
      tournaments.value = rangResult.data
      totalTournaments.value = rangResult.data.length
    } else {
      const result = await tournamentsAPI.getTournaments(params)
      tournaments.value = result.data
      // В реальном API должен быть total count в ответе
      totalTournaments.value = result.data.length
    }

    // Дополнительная фильтрация на клиенте
    if (!showArchived.value) {
      tournaments.value = tournaments.value.filter(t => !t.archived)
    }

    if (dateFilter.value !== 'all') {
      const now = new Date()
      tournaments.value = tournaments.value.filter(t => {
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
  } catch (error) {
    console.error('Ошибка при загрузке турниров:', error)
    alert('Не удалось загрузить турниры')
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
  }
}

const handleSearch = (query: string) => {
  searchQuery.value = query
  selectedRangId.value = null
  loadTournaments()
}

const handleFilterByRang = (rangId: number | null) => {
  selectedRangId.value = rangId
  searchQuery.value = ''
  loadTournaments()
}

const handleSort = (field: string, direction: string) => {
  sortField.value = field
  sortDirection.value = direction
  loadTournaments()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadTournaments()
}

const resetFilters = () => {
  searchQuery.value = ''
  selectedRangId.value = null
  sortField.value = 'startDate'
  sortDirection.value = 'desc'
  showArchived.value = false
  dateFilter.value = 'all'
  currentPage.value = 1
  loadTournaments()
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
      // Редактирование существующего турнира
      await tournamentsAPI.updateTournament(editingTournament.value.id, tournamentData)
      alert('Турнир успешно обновлен!')
    } else {
      // Создание нового турнира
      await tournamentsAPI.createTournament(tournamentData)
      alert('Турнир успешно создан!')
    }

    closeFormModal()
    loadTournaments()
  } catch (error: any) {
    const message = error.response?.data?.message || 'Ошибка при сохранении турнира'
    alert(message)
  }
}

const deleteTournament = async (tournament: Tournament) => {
  if (!confirm(`Вы уверены, что хотите удалить турнир "${tournament.name}"?`)) {
    return
  }

  try {
    await tournamentsAPI.deleteTournament(tournament.id)
    alert('Турнир успешно удален!')
    loadTournaments()
  } catch (error: any) {
    const message = error.response?.data?.message || 'Ошибка при удалении турнира'
    alert(message)
  }
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
  color: #2d3748;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #718096;
}

.box {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

.box h3 {
  color: #4a5568;
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
  max-width: 800px;
  border-radius: 12px;
  overflow: hidden;
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
}
</style>
