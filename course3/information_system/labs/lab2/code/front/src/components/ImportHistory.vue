<template>
  <div class="import-history">
    <h2>История импорта</h2>

    <div class="filters">
      <div class="search-box">
        <input
            type="text"
            v-model="searchQuery"
            placeholder="Поиск по файлу или статусу..."
            @input="filterHistory"
        />
        <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="#718096">
          <path d="M11 2c4.968 0 9 4.032 9 9s-4.032 9-9 9-9-4.032-9-9 4.032-9 9-9zm0 16c3.867 0 7-3.133 7-7 0-3.868-3.133-7-7-7-3.868 0-7 3.132-7 7 0 3.867 3.132 7 7 7zm8.485.071l2.829 2.828-1.415 1.415-2.828-2.829 1.414-1.414z"/>
        </svg>
      </div>

      <div class="date-filter">
        <label>С:</label>
        <input type="date" v-model="startDate" @change="filterHistory" />

        <label>По:</label>
        <input type="date" v-model="endDate" @change="filterHistory" />
      </div>
    </div>

    <div class="history-table">
      <div class="table-header">
        <div class="header-cell">ID</div>
        <div class="header-cell">Дата</div>
        <div class="header-cell">Файл</div>
        <div class="header-cell">Пользователь</div>
        <div class="header-cell">Статус</div>
        <div class="header-cell">Записей</div>
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
            <div class="cell">{{item.id}}</div>
            <div class="cell">{{ formatDate(item.importDate) }}</div>
            <div class="cell file-cell">
              <svg class="file-icon" width="16" height="16" viewBox="0 0 24 24" fill="#667eea">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
              {{ item.fileName }}
            </div>
            <div class="cell">{{ item.user }}</div>
            <div class="cell">
              <span :class="`status-badge status-${item.status.toLowerCase()}`">
                {{ getStatusText(item.status) }}
              </span>
            </div>
            <div class="cell">{{ item.recordsAdded || 0 }}</div>
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
            <span>{{ formatDate(selectedItem.importDate) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Пользователь:</span>
            <span>{{ selectedItem.user }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Статус:</span>
            <span :class="`status-badge status-${selectedItem.status.toLowerCase()}`">
              {{ getStatusText(selectedItem.status) }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Записей добавлено:</span>
            <span>{{ selectedItem.recordsAdded || 0 }}</span>
          </div>
          <div v-if="selectedItem.errorMessage" class="detail-row">
            <span class="detail-label">Ошибка:</span>
            <span class="error-text">{{ selectedItem.errorMessage }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ImportHistory',
  data() {
    return {
      searchQuery: '',
      startDate: '',
      endDate: '',
      loading: false,
      selectedItem: null,
      importHistory: [],
      originalHistory: [] // Сохраняем оригинальные данные
    }
  },
  computed: {
    filteredHistory() {
      let filtered = this.importHistory;

      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        filtered = filtered.filter(item =>
            (item.fileName && item.fileName.toLowerCase().includes(query)) ||
            (item.user && item.user.toLowerCase().includes(query)) ||
            this.getStatusText(item.status).toLowerCase().includes(query)
        );
      }

      if (this.startDate) {
        const start = new Date(this.startDate);
        filtered = filtered.filter(item => {
          const itemDate = new Date(item.importDate);
          return itemDate >= start;
        });
      }

      if (this.endDate) {
        const end = new Date(this.endDate);
        end.setHours(23, 59, 59, 999); // Устанавливаем конец дня
        filtered = filtered.filter(item => {
          const itemDate = new Date(item.importDate);
          return itemDate <= end;
        });
      }

      return filtered.sort((a, b) => new Date(b.importDate) - new Date(a.importDate));
    }
  },
  methods: {
    formatDate(date) {
      if (!date) return 'Н/Д';
      const d = new Date(date);
      return d.toLocaleDateString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    },

    getStatusText(status) {
      const statusMap = {
        'SUCCESS': 'Завершено',
        'PROCESSING': 'В обработке',
        'FAILED': 'Ошибка'
      };
      return statusMap[status] || status;
    },

    viewDetails(item) {
      this.selectedItem = { ...item };
    },

    closeModal() {
      this.selectedItem = null;
    },

    downloadReport(item) {
      console.log('Скачивание отчета для:', item.fileName);
      // Временная заглушка
      alert(`Функция скачивания отчета для файла "${item.fileName}" будет реализована позже`);
    },

    filterHistory() {
      // Фильтрация уже выполняется в computed свойстве
      console.log('Фильтрация обновлена');
    },

    async loadHistory() {
      const token = localStorage.getItem('authToken');
      if (!token) {
        console.error('Требуется авторизация');
        this.$router.push('/login');
        return;
      }

      this.loading = true;
      try {
        // Убедитесь, что URL правильный (совпадает с вашим API)
        const response = await axios.get('/api/import/history', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        console.log('Ответ от сервера:', response.data);

        if (response.data && response.data.history) {
          this.importHistory = response.data.history;
          this.originalHistory = [...response.data.history]; // Сохраняем копию
        } else {
          this.importHistory = [];
          this.originalHistory = [];
          console.warn('История импорта пуста или не в ожидаемом формате');
        }

      } catch (error) {
        console.error('Ошибка загрузки истории:', error);

        // Показываем подробную информацию об ошибке
        if (error.response) {
          console.error('Статус ошибки:', error.response.status);
          console.error('Данные ошибки:', error.response.data);

          if (error.response.status === 401) {
            alert('Сессия истекла. Пожалуйста, войдите снова.');
            this.$router.push('/login');
          } else if (error.response.status === 403) {
            alert('У вас нет прав для просмотра истории импорта.');
          }
        } else if (error.request) {
          console.error('Запрос был сделан, но ответ не получен:', error.request);
          alert('Ошибка сети. Проверьте соединение с сервером.');
        }

        this.importHistory = [];
        this.originalHistory = [];
      } finally {
        this.loading = false;
      }
    }
  },
  mounted() {
    this.loadHistory();

    // Для отладки: проверяем URL API
    console.log('Текущий URL API:', window.location.origin + '/api/import/history');
  }
}
</script>

<style scoped>
/* Стили оставить без изменений, они уже хорошие */
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
  grid-template-columns: 1.5fr 2fr 1.5fr 1fr 1fr 1.5fr;
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
  font-style: italic;
}

.history-item {
  display: grid;
  grid-template-columns: 1.5fr 2fr 1.5fr 1fr 1fr 1.5fr;
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

.status-success {
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
  flex: 1;
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
}
</style>
