<template>
  <div class="tournament-table">
    <!-- Фильтры и поиск -->
    <div class="level mb-4">
      <div class="level-left">
        <div class="level-item">
          <div class="field has-addons">
            <div class="control">
              <input
                v-model="searchQuery"
                class="input"
                type="text"
                placeholder="Поиск по названию..."
                @input="onSearch"
              >
            </div>
            <div class="control">
              <button class="button is-primary" @click="onSearch">
                <span class="icon">
                  <i class="fas fa-search"></i>
                </span>
              </button>
            </div>
          </div>
        </div>

        <div class="level-item">
          <div class="field">
            <div class="control">
              <div class="select">
                <select v-model="selectedRang" @change="onRangChange">
                  <option value="">Все ранги</option>
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
          </div>
        </div>
      </div>

      <div class="level-right">
        <div class="level-item">
          <button
            v-if="authStore.isAuthenticated && hasPermission('PublishTournament')"
            class="button is-primary"
            @click="$emit('create')"
          >
            <span class="icon">
              <i class="fas fa-plus"></i>
            </span>
            <span>Создать турнир</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Таблица -->
    <div class="table-container">
      <table class="table is-fullwidth is-striped is-hoverable">
        <thead>
        <tr>
          <th @click="sortBy('name')" class="is-clickable">
            Название
            <span class="icon is-small" v-if="sort.field === 'name'">
                <i :class="sort.direction === 'asc' ? 'fas fa-sort-up' : 'fas fa-sort-down'"></i>
              </span>
          </th>
          <th @click="sortBy('startDate')" class="is-clickable">
            Дата начала
            <span class="icon is-small" v-if="sort.field === 'startDate'">
                <i :class="sort.direction === 'asc' ? 'fas fa-sort-up' : 'fas fa-sort-down'"></i>
              </span>
          </th>
          <th @click="sortBy('finishDate')" class="is-clickable">
            Дата окончания
            <span class="icon is-small" v-if="sort.field === 'finishDate'">
                <i :class="sort.direction === 'asc' ? 'fas fa-sort-up' : 'fas fa-sort-down'"></i>
              </span>
          </th>
          <th>Адрес</th>
          <th>Ранг</th>
          <th>Мин. возраст</th>
          <th>Статус</th>
          <th v-if="showActions">Действия</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="loading">
          <td colspan="8" class="has-text-centered">
            <i class="fas fa-spinner fa-spin fa-lg"></i>
            <span class="ml-2">Загрузка турниров...</span>
          </td>
        </tr>

        <tr v-else-if="tournaments.length === 0">
          <td colspan="8" class="has-text-centered">
            <p class="has-text-grey">Турниры не найдены</p>
          </td>
        </tr>

        <tr
          v-for="tournament in tournaments"
          :key="tournament.id"
          :class="{ 'has-background-grey-light': tournament.archived }"
          class="is-clickable"
          @click="$emit('view', tournament)"
        >
          <td>
            <strong>{{ tournament.name }}</strong>
            <a
              v-if="tournament.link"
              :href="tournament.link"
              target="_blank"
              class="icon ml-2"
              @click.stop
            >
              <i class="fas fa-external-link-alt has-text-info"></i>
            </a>
          </td>
          <td>{{ formatDate(tournament.startDate) }}</td>
          <td>{{ formatDate(tournament.finishDate) }}</td>
          <td>{{ tournament.address }}</td>
          <td>
              <span class="tag" :class="getRangColor(tournament.rangId)">
                {{ getRangName(tournament.rangId) }}
              </span>
          </td>
          <td>
              <span class="tag is-light">
                {{ tournament.minimalAge }} лет
              </span>
          </td>
          <td>
              <span
                class="tag"
                :class="tournament.archived ? 'is-dark' : 'is-success'"
              >
                {{ tournament.archived ? 'Архивный' : 'Активный' }}
              </span>
          </td>
          <td v-if="showActions">
            <div class="buttons are-small">
              <button
                v-if="hasPermission('EditTournament')"
                class="button is-info is-light"
                @click.stop="$emit('edit', tournament)"
              >
                  <span class="icon">
                    <i class="fas fa-edit"></i>
                  </span>
              </button>
              <button
                v-if="hasPermission('DeleteTournament')"
                class="button is-danger is-light"
                @click.stop="confirmDelete(tournament)"
              >
                  <span class="icon">
                    <i class="fas fa-trash"></i>
                  </span>
              </button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Пагинация -->
    <nav v-if="totalPages > 1" class="pagination is-centered mt-4" role="navigation">
      <ul class="pagination-list">
        <li>
          <button
            class="pagination-previous"
            :disabled="currentPage === 1"
            @click="changePage(currentPage - 1)"
          >
            Назад
          </button>
        </li>

        <li v-for="page in pages" :key="page">
          <button
            class="pagination-link"
            :class="{ 'is-current': page === currentPage }"
            @click="changePage(page)"
          >
            {{ page }}
          </button>
        </li>

        <li>
          <button
            class="pagination-next"
            :disabled="currentPage === totalPages"
            @click="changePage(currentPage + 1)"
          >
            Вперед
          </button>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../../stores/auth'
import type { Tournament, Rang } from '../../types/tournaments'

interface Props {
  tournaments: Tournament[]
  rangs: Rang[]
  loading: boolean
  totalItems?: number
  itemsPerPage?: number
  showActions?: boolean
}

interface Emits {
  (e: 'search', query: string): void
  (e: 'filter-by-rang', rangId: number | null): void
  (e: 'sort', field: string, direction: string): void
  (e: 'page-change', page: number): void
  (e: 'view', tournament: Tournament): void
  (e: 'edit', tournament: Tournament): void
  (e: 'delete', tournament: Tournament): void
  (e: 'create'): void
}

const props = withDefaults(defineProps<Props>(), {
  totalItems: 0,
  itemsPerPage: 10,
  showActions: true
})

const emit = defineEmits<Emits>()

const authStore = useAuthStore()
const searchQuery = ref('')
const selectedRang = ref<string>('')
const sort = ref({
  field: 'startDate',
  direction: 'desc'
})
const currentPage = ref(1)

const hasPermission = (permission: string) => {
  if (!authStore.user) return false
  return authStore.user.roles.includes(`OAPI:ROLE:${permission}`)
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ru-RU')
}

const getRangName = (rangId: number) => {
  const rang = props.rangs.find(r => r.id === rangId)
  return rang ? rang.name : 'Неизвестно'
}

const getRangColor = (rangId: number) => {
  const colors = ['is-primary', 'is-success', 'is-info', 'is-warning', 'is-danger']
  return colors[rangId % colors.length]
}

const onSearch = () => {
  currentPage.value = 1
  emit('search', searchQuery.value)
}

const onRangChange = () => {
  currentPage.value = 1
  const rangId = selectedRang.value ? parseInt(selectedRang.value) : null
  emit('filter-by-rang', rangId)
}

const sortBy = (field: string) => {
  if (sort.value.field === field) {
    sort.value.direction = sort.value.direction === 'asc' ? 'desc' : 'asc'
  } else {
    sort.value.field = field
    sort.value.direction = 'asc'
  }
  emit('sort', sort.value.field, sort.value.direction)
}

const totalPages = computed(() => {
  return Math.ceil(props.totalItems / props.itemsPerPage)
})

const pages = computed(() => {
  const pagesArray = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pagesArray.push(i)
  }

  return pagesArray
})

const changePage = (page: number) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  emit('page-change', page)
}

const confirmDelete = (tournament: Tournament) => {
  if (confirm(`Вы уверены, что хотите удалить турнир "${tournament.name}"?`)) {
    emit('delete', tournament)
  }
}

// Сброс при смене данных
onMounted(() => {
  currentPage.value = 1
})
</script>

<style scoped>
.tournament-table {
  background: #252e2e;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.table-container {
  overflow-x: auto;
}

.table {
  min-width: 1000px;
}

.table th {
  background-color: #3e4736;
  font-weight: 600;
  color: #fdfcfc;
  border-bottom: 2px solid #dee2e6;
}

.table td {
  vertical-align: middle;
  border-bottom: 1px solid #114e8f;
}

.table tbody tr:hover {
  background-color: rgba(60, 157, 248, 0.27) !important;
}

.tag {
  border-radius: 4px;
  font-weight: 500;
}

.is-clickable {
  cursor: pointer;
}

.is-clickable:hover {
  background-color: #f0f7ff;
}

.pagination-link.is-current {
  background-color: #667eea;
  border-color: #667eea;
  color: white;
}

.pagination-link,
.pagination-previous,
.pagination-next {
  border-color: #e1e5e9;
}

.pagination-link:hover,
.pagination-previous:hover,
.pagination-next:hover {
  border-color: #667eea;
  color: #667eea;
}

@media (max-width: 768px) {
  .tournament-table {
    padding: 1rem;
  }

  .level {
    flex-direction: column;
    align-items: stretch;
  }

  .level-left,
  .level-right {
    margin-bottom: 1rem;
  }
}
</style>
