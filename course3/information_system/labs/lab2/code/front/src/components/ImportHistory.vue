<template>
  <div class="import-history">
    <h2>История импорта</h2>

    <div class="filters">
      <div class="search-box">
        <input
            type="text"
            v-model="searchQuery"
            placeholder="Поиск по файлу или статусу..."
        />
        <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="#718096">
          <path d="M11 2c4.968 0 9 4.032 9 9s-4.032 9-9 9-9-4.032-9-9 4.032-9 9-9zm0 16c3.867 0 7-3.133 7-7 0-3.868-3.133-7-7-7-3.868 0-7 3.132-7 7 0 3.867 3.132 7 7 7zm8.485.071l2.829 2.828-1.415 1.415-2.828-2.829 1.414-1.414z"/>
        </svg>
      </div>

      <div class="date-filter">
        <label>С:</label>
        <input type="date" v-model="startDate" />

        <label>По:</label>
        <input type="date" v-model="endDate" />
      </div>
    </div>

    <div class="history-table">
      <div class="table-header">
        <div class="header-cell">Дата</div>
        <div class="header-cell">Файл</div>
        <div class="header-cell">Пользователь</div>
        <div class="header-cell">Статус</div>
        <div class="header-cell">Действия</div>
      </div>

      <div class="table-body">
        <div v-if="loading" class="loading">
          Загрузка истории...
        </div>

        <div v-else-if="filteredHistory.length === 0" class="no-data">
          История импорта отсутствует
        </div>

        <div v-else class="history-items">
          <div v-for="item in filteredHistory" :key="item.id" class="history-item">
            <div class="cell">{{ formatDate(item.date) }}</div>
            <div class="cell file-cell">
              <svg class="file-icon" width="16" height="16" viewBox="0 0 24 24" fill="#667eea">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              {{ item.fileName }}
            </div>
            <div class="cell">{{ item.user }}</div>
            <div class="cell">
              <span :class="`status-badge status-${item.status}`">
                {{ getStatusText(item.status) }}
              </span>
            </div>
            <div class="cell actions-cell">
              <button @click="viewDetails(item)" class="view-btn">
                Просмотр
              </button>
              <button v-if="item.status === 'completed'" @click="downloadReport(item)" class="download-btn">
                Отчет
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно деталей -->
    <div v-if="selectedItem" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Детали импорта</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-row">
            <span class="detail-label">Файл:</span>
            <span>{{ selectedItem.fileName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Дата импорта:</span>
            <span>{{ formatDate(selectedItem.date) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Пользователь:</span>
            <span>{{ selectedItem.user }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Статус:</span>
            <span :class="`status-badge status-${selectedItem.status}`">
              {{ getStatusText(selectedItem.status) }}
            </span>
          </div>
          <div v-if="selectedItem.errorMessage" class="detail-row">
            <span class="detail-label">Ошибка:</span>
            <span class="error-text">{{ selectedItem.errorMessage }}</span>
          </div>
          <div v-if="selectedItem.stats" class="stats">
            <h4>Статистика:</h4>
            <div class="stats-grid">
              <div class="stat-item">
                <span class="stat-value">{{ selectedItem.stats.total || 0 }}</span>
                <span class="stat-label">Всего записей</span>
              </div>
              <div class="stat-item">
                <span class="stat-value success">{{ selectedItem.stats.success || 0 }}</span>
                <span class="stat-label">Успешно</span>
              </div>
              <div class="stat-item">
                <span class="stat-value error">{{ selectedItem.stats.failed || 0 }}</span>
                <span class="stat-label">С ошибками</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ImportHistory',
  data() {
    return {
      searchQuery: '',
      startDate: '',
      endDate: '',
      loading: false,
      selectedItem: null,
      // Временные данные для демонстрации
      importHistory: [
        {
          id: 1,
          date: new Date('2024-01-15'),
          fileName: 'organizations_01.xml',
          user: 'Иванов И.И.',
          status: 'completed',
          stats: { total: 150, success: 145, failed: 5 }
        },
        {
          id: 2,
          date: new Date('2024-01-14'),
          fileName: 'data_import.xml',
          user: 'Петров П.П.',
          status: 'failed',
          errorMessage: 'Неверный формат XML',
          stats: { total: 0, success: 0, failed: 0 }
        },
        {
          id: 3,
          date: new Date('2024-01-13'),
          fileName: 'companies.xml',
          user: 'Сидорова С.С.',
          status: 'processing',
          stats: { total: 80, success: 0, failed: 0 }
        },
        {
          id: 4,
          date: new Date('2024-01-12'),
          fileName: 'departments.xml',
          user: 'Алексеев А.А.',
          status: 'completed',
          stats: { total: 45, success: 45, failed: 0 }
        }
      ]
    }
  },
  computed: {
    filteredHistory() {
      let filtered = this.importHistory

      // Фильтр по поисковому запросу
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        filtered = filtered.filter(item =>
            item.fileName.toLowerCase().includes(query) ||
            item.user.toLowerCase().includes(query) ||
            this.getStatusText(item.status).toLowerCase().includes(query)
        )
      }

      // Фильтр по дате
      if (this.startDate) {
        const start = new Date(this.startDate)
        filtered = filtered.filter(item => new Date(item.date) >= start)
      }

      if (this.endDate) {
        const end = new Date(this.endDate)
        filtered = filtered.filter(item => new Date(item.date) <= end)
      }

      return filtered.sort((a, b) => new Date(b.date) - new Date(a.date))
    }
  },
  methods: {
    formatDate(date) {
      return new Date(date).toLocaleDateString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    getStatusText(status) {
      const statusMap = {
        'completed': 'Завершено',
        'processing': 'В обработке',
        'failed': 'Ошибка',
        'pending': 'Ожидает'
      }
      return statusMap[status] || status
    },

    viewDetails(item) {
      this.selectedItem = { ...item }
    },

    closeModal() {
      this.selectedItem = null
    },

    downloadReport(item) {
      // Заглушка для скачивания отчета
      console.log('Скачивание отчета для:', item.fileName)
      alert('Функция скачивания отчета будет реализована после подключения бэкенда')
    },

    async loadHistory() {
      this.loading = true
      try {
        // Здесь будет вызов API для получения истории
        await new Promise(resolve => setTimeout(resolve, 500))
      } catch (error) {
        console.error('Ошибка загрузки истории:', error)
      } finally {
        this.loading = false
      }
    }
  },
  mounted() {
    this.loadHistory()
  }
}
</script>

<style scoped>
.import-history {
  padding: 20px;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

.filters {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 20px;
}

.search-box {
  position: relative;
  flex: 1;
  max-width: 400px;
}

.search-box input {
  width: 100%;
  padding: 10px 40px 10px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.search-box input:focus {
  outline: none;
  border-color: #667eea;
}

.search-icon {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
}

.date-filter {
  display: flex;
  align-items: center;
  gap: 10px;
}

.date-filter label {
  color: #4a5568;
  font-weight: 500;
}

.date-filter input {
  padding: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
}

.history-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.table-header {
  display: grid;
  grid-template-columns: 1fr 2fr 1.5fr 1fr 1.5fr;
  background: #667eea;
  color: white;
  font-weight: 600;
}

.header-cell {
  padding: 15px;
  text-align: left;
}

.table-body {
  min-height: 300px;
}

.loading, .no-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #718096;
}

.history-item {
  display: grid;
  grid-template-columns: 1fr 2fr 1.5fr 1fr 1.5fr;
  border-bottom: 1px solid #e2e8f0;
  transition: background 0.2s ease;
}

.history-item:hover {
  background: #f8faff;
}

.cell {
  padding: 15px;
  display: flex;
  align-items: center;
  color: #4a5568;
}

.file-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-completed {
  background: #c6f6d5;
  color: #276749;
}

.status-processing {
  background: #fed7d7;
  color: #c53030;
}

.status-failed {
  background: #feebc8;
  color: #c05621;
}

.status-pending {
  background: #e2e8f0;
  color: #4a5568;
}

.actions-cell {
  display: flex;
  gap: 10px;
}

.view-btn, .download-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.view-btn {
  background: #667eea;
  color: white;
}

.view-btn:hover {
  background: #5a67d8;
}

.download-btn {
  background: #48bb78;
  color: white;
}

.download-btn:hover {
  background: #38a169;
}

/* Модальное окно */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #718096;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-btn:hover {
  background: #f7fafc;
  color: #4a5568;
}

.modal-body {
  padding: 20px;
}

.detail-row {
  display: flex;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f7fafc;
}

.detail-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.detail-label {
  font-weight: 600;
  color: #2d3748;
  min-width: 150px;
}

.error-text {
  color: #c53030;
  font-weight: 500;
}

.stats {
  margin-top: 20px;
  padding: 20px;
  background: #f7fafc;
  border-radius: 8px;
}

.stats h4 {
  margin: 0 0 15px 0;
  color: #2d3748;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 5px;
}

.stat-value.success {
  color: #48bb78;
}

.stat-value.error {
  color: #f56565;
}

.stat-label {
  font-size: 12px;
  color: #718096;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (max-width: 768px) {
  .table-header,
  .history-item {
    grid-template-columns: 1fr;
  }

  .header-cell,
  .cell {
    padding: 10px;
    border-bottom: 1px solid #e2e8f0;
  }

  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: 100%;
  }

  .date-filter {
    flex-wrap: wrap;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>