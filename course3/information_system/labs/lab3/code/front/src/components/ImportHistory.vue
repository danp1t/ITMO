<template>
  <div class="import-history">
    <div class="page-header">
      <h2>История импорта организаций</h2>
      <div class="header-actions">
        <button @click="refreshHistory" class="refresh-btn" :disabled="loading">
          <svg class="refresh-icon" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
          </svg>
          <span>Обновить</span>
        </button>
      </div>
    </div>

    <div class="filters-section">
      <div class="filters-row">
        <div class="search-filter">
          <div class="input-with-icon">
            <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="#718096">
              <path d="M11 2c4.968 0 9 4.032 9 9s-4.032 9-9 9-9-4.032-9-9 4.032-9 9-9zm0 16c3.867 0 7-3.133 7-7 0-3.868-3.133-7-7-7-3.868 0-7 3.132-7 7 0 3.867 3.132 7 7 7zm8.485.071l2.829 2.828-1.415 1.415-2.828-2.829 1.414-1.414z"/>
            </svg>
            <input
                type="text"
                v-model="searchQuery"
                placeholder="Поиск по названию файла, пользователю или статусу..."
                @input="debouncedFilterHistory"
                class="search-input"
            />
          </div>
        </div>

        <div class="date-filters">
          <div class="date-filter-group">
            <label class="date-label">Дата с:</label>
            <input
                type="date"
                v-model="startDate"
                @change="filterHistory"
                class="date-input"
            />
          </div>

          <div class="date-filter-group">
            <label class="date-label">Дата по:</label>
            <input
                type="date"
                v-model="endDate"
                @change="filterHistory"
                class="date-input"
            />
          </div>
        </div>

        <div class="status-filters">
          <label class="filter-label">Статус:</label>
          <select v-model="statusFilter" @change="filterHistory" class="status-select">
            <option value="">Все статусы</option>
            <option value="SUCCESS">Успешно</option>
            <option value="FAILED">С ошибкой</option>
            <option value="PROCESSING">В обработке</option>
          </select>
        </div>

        <div class="user-filter" v-if="userRole === 'ADMIN'">
          <label class="filter-label">Пользователь:</label>
          <select v-model="userFilter" @change="filterHistory" class="user-select">
            <option value="">Все пользователи</option>
            <option v-for="user in uniqueUsers" :key="user" :value="user">
              {{ user }}
            </option>
          </select>
        </div>
      </div>

      <div class="filter-summary" v-if="filteredHistory.length !== originalHistory.length">
        Найдено {{ filteredHistory.length }} из {{ originalHistory.length }} записей
        <button @click="clearFilters" class="clear-filters-btn">
          Очистить фильтры
        </button>
      </div>
    </div>

    <div class="history-table-container">
      <div class="table-wrapper">
        <div class="table-header">
          <div class="header-cell" @click="sortBy('id')">
            ID
            <span class="sort-icon" v-if="sortField === 'id'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="header-cell" @click="sortBy('importDate')">
            Дата импорта
            <span class="sort-icon" v-if="sortField === 'importDate'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="header-cell" @click="sortBy('fileName')">
            Файл
            <span class="sort-icon" v-if="sortField === 'fileName'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="header-cell" @click="sortBy('user')" v-if="userRole === 'ADMIN'">
            Пользователь
            <span class="sort-icon" v-if="sortField === 'user'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="header-cell" @click="sortBy('status')">
            Статус
            <span class="sort-icon" v-if="sortField === 'status'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="header-cell" @click="sortBy('recordsAdded')">
            Записей
            <span class="sort-icon" v-if="sortField === 'recordsAdded'">
              {{ sortDirection === 'asc' ? '↑' : '↓' }}
            </span>
          </div>
          <div class="header-cell actions-header">
            Действия
          </div>
        </div>

        <div class="table-body">
          <div v-if="loading" class="loading-state">
            <div class="spinner"></div>
            <p>Загрузка истории импорта...</p>
          </div>

          <div v-else-if="filteredHistory.length === 0 && !loading" class="empty-state">
            <svg class="empty-icon" width="64" height="64" viewBox="0 0 24 24" fill="#cbd5e0">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
            <p v-if="originalHistory.length === 0">
              История импорта пуста. Загрузите первый XML файл.
            </p>
            <p v-else>
              По вашему запросу ничего не найдено. Попробуйте изменить фильтры.
            </p>
          </div>

          <div v-else class="history-items">
            <div
                v-for="item in paginatedHistory"
                :key="item.id"
                class="history-item"
                :class="{
                'item-success': item.status === 'SUCCESS',
                'item-failed': item.status === 'FAILED',
                'item-processing': item.status === 'PROCESSING'
              }"
            >
              <div class="cell cell-id">{{ item.id }}</div>
              <div class="cell cell-date">
                <div class="date-time">{{ formatDate(item.importDate) }}</div>
                <div class="time-ago">{{ formatTimeAgo(item.importDate) }}</div>
              </div>
              <div class="cell cell-file">
                <div class="file-info">
                  <svg class="file-icon" width="20" height="20" viewBox="0 0 24 24" fill="#667eea">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                  </svg>
                  <div class="file-details">
                    <div class="file-name" :title="item.fileName">
                      {{ truncateFileName(item.fileName) }}
                    </div>
                    <div class="file-meta" v-if="item.fileSize">
                      {{ formatFileSize(item.fileSize) }}
                    </div>
                  </div>
                </div>
              </div>
              <div class="cell cell-user" v-if="userRole === 'ADMIN'">
                <div class="user-badge">
                  <svg class="user-icon" width="16" height="16" viewBox="0 0 24 24" fill="#718096">
                    <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                  </svg>
                  {{ item.user }}
                </div>
              </div>
              <div class="cell cell-status">
                <span :class="`status-badge status-${item.status.toLowerCase()}`">
                  {{ getStatusText(item.status) }}
                  <span v-if="item.status === 'PROCESSING'" class="processing-dots">
                    <span class="dot">.</span>
                    <span class="dot">.</span>
                    <span class="dot">.</span>
                  </span>
                </span>
              </div>
              <div class="cell cell-records">
                <div class="records-count" :class="{'zero-records': !item.recordsAdded}">
                  {{ item.recordsAdded || 0 }}
                </div>
              </div>
              <div class="cell cell-actions">
                <div class="action-buttons">
                  <button
                      @click="viewDetails(item)"
                      class="action-btn view-btn"
                      title="Просмотреть детали"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                    </svg>
                  </button>

                  <button
                      v-if="(item.status === 'SUCCESS' || item.status === 'FAILED') && item.fileKey"
                      @click="downloadFile(item)"
                      :disabled="item.downloading"
                      class="action-btn download-btn"
                      :title="`Скачать файл: ${item.fileName}`"
                  >
                    <svg
                        v-if="!item.downloading"
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="currentColor"
                    >
                      <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
                    </svg>
                    <div v-else class="download-spinner"></div>
                  </button>

                  <button
                      v-if="item.errorMessage"
                      @click="viewErrorDetails(item)"
                      class="action-btn error-btn"
                      title="Показать ошибку"
                  >
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="filteredHistory.length > 0 && !loading">
        <div class="pagination-info">
          Показано {{ startIndex + 1 }}-{{ Math.min(endIndex, filteredHistory.length) }} из {{ filteredHistory.length }}
        </div>
        <div class="pagination-controls">
          <button
              @click="prevPage"
              :disabled="currentPage === 1"
              class="pagination-btn"
          >
            ←
          </button>

          <div class="page-numbers">
            <button
                v-for="page in visiblePages"
                :key="page"
                @click="goToPage(page)"
                :class="{ active: page === currentPage }"
                class="page-btn"
            >
              {{ page }}
            </button>
            <span v-if="totalPages > 5 && currentPage < totalPages - 2">...</span>
          </div>

          <button
              @click="nextPage"
              :disabled="currentPage === totalPages"
              class="pagination-btn"
          >
            →
          </button>
        </div>

        <div class="page-size-selector">
          <label>На странице:</label>
          <select v-model="pageSize" @change="resetPagination">
            <option value="10">10</option>
            <option value="25">25</option>
            <option value="50">50</option>
            <option value="100">100</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Модальное окно деталей операции -->
    <div v-if="selectedItem" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Детали операции импорта</h3>
          <button @click="closeModal" class="close-btn" title="Закрыть">×</button>
        </div>

        <div class="modal-body">
          <div class="detail-section">
            <h4>Основная информация</h4>
            <div class="detail-grid">
              <div class="detail-row">
                <span class="detail-label">ID операции:</span>
                <span class="detail-value">{{ selectedItem.id }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Файл:</span>
                <span class="detail-value file-value">
                  <svg class="file-icon-small" width="16" height="16" viewBox="0 0 24 24" fill="#667eea">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                  </svg>
                  {{ selectedItem.fileName }}
                  <span v-if="selectedItem.fileSize" class="file-size-small">
                    ({{ formatFileSize(selectedItem.fileSize) }})
                  </span>
                </span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Дата импорта:</span>
                <span class="detail-value">{{ formatDate(selectedItem.importDate) }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Пользователь:</span>
                <span class="detail-value">
                  <svg class="user-icon-small" width="14" height="14" viewBox="0 0 24 24" fill="#718096">
                    <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                  </svg>
                  {{ selectedItem.user }}
                  <span v-if="userRole === 'ADMIN'" class="user-id">
                    (ID: {{ selectedItem.userId }})
                  </span>
                </span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Статус:</span>
                <span :class="`status-badge-large status-${selectedItem.status.toLowerCase()}`">
                  {{ getStatusText(selectedItem.status) }}
                </span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Записей добавлено:</span>
                <span class="detail-value records-value">
                  {{ selectedItem.recordsAdded || 0 }}
                </span>
              </div>
            </div>
          </div>

          <div class="detail-section" v-if="selectedItem.errorMessage">
            <h4>Информация об ошибке</h4>
            <div class="error-details">
              <div class="error-message">
                {{ selectedItem.errorMessage }}
              </div>
              <div class="error-time" v-if="selectedItem.errorTime">
                Время ошибки: {{ formatDate(selectedItem.errorTime) }}
              </div>
            </div>
          </div>

          <div class="detail-section" v-if="selectedItem.fileKey">
            <h4>Файл импорта</h4>
            <div class="file-actions">
              <button
                  @click="downloadFromModal(selectedItem)"
                  :disabled="selectedItem.downloading"
                  class="download-file-btn"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
                </svg>
                <span>Скачать оригинальный файл</span>
                <div v-if="selectedItem.downloading" class="modal-download-spinner"></div>
              </button>

              <div class="file-info-details">
                <div class="file-info-item">
                  <span class="info-label">Ключ файла:</span>
                  <span class="info-value file-key" :title="selectedItem.fileKey">
                    {{ truncateFileKey(selectedItem.fileKey) }}
                  </span>
                </div>
                <div class="file-info-item" v-if="selectedItem.fileSize">
                  <span class="info-label">Размер:</span>
                  <span class="info-value">{{ formatFileSize(selectedItem.fileSize) }}</span>
                </div>
                <div class="file-info-item">
                  <span class="info-label">Тип:</span>
                  <span class="info-value">XML</span>
                </div>
              </div>
            </div>
          </div>

          <div class="detail-section" v-if="selectedItem.transactionInfo">
            <h4>Транзакционная информация</h4>
            <div class="transaction-details">
              <div class="transaction-item">
                <span class="tx-label">Статус транзакции:</span>
                <span class="tx-value" :class="`tx-${selectedItem.transactionInfo.status}`">
                  {{ selectedItem.transactionInfo.status }}
                </span>
              </div>
              <div class="transaction-item" v-if="selectedItem.transactionInfo.fileStorageStatus">
                <span class="tx-label">Хранилище файлов:</span>
                <span class="tx-value">{{ selectedItem.transactionInfo.fileStorageStatus }}</span>
              </div>
              <div class="transaction-item" v-if="selectedItem.transactionInfo.duration">
                <span class="tx-label">Длительность:</span>
                <span class="tx-value">{{ selectedItem.transactionInfo.duration }} мс</span>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeModal" class="modal-close-btn">Закрыть</button>
          <button
              v-if="selectedItem.fileKey && (selectedItem.status === 'SUCCESS' || selectedItem.status === 'FAILED')"
              @click="downloadFromModal(selectedItem)"
              :disabled="selectedItem.downloading"
              class="modal-download-btn"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
            </svg>
            Скачать файл
          </button>
        </div>
      </div>
    </div>

    <!-- Уведомления -->
    <div v-if="notification.message" class="notification" :class="notification.type">
      <span>{{ notification.message }}</span>
      <button @click="clearNotification" class="notification-close">×</button>
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
      statusFilter: '',
      userFilter: '',
      loading: false,
      selectedItem: null,
      importHistory: [],
      originalHistory: [],
      userRole: '',
      currentPage: 1,
      pageSize: 10,
      sortField: 'importDate',
      sortDirection: 'desc',
      notification: {
        message: '',
        type: 'info'
      },
      debounceTimer: null
    }
  },
  computed: {
    uniqueUsers() {
      const users = [...new Set(this.originalHistory.map(item => item.user))];
      return users.sort();
    },

    stats() {
      const stats = {
        total: this.originalHistory.length,
        success: this.originalHistory.filter(item => item.status === 'SUCCESS').length,
        failed: this.originalHistory.filter(item => item.status === 'FAILED').length,
        totalRecords: this.originalHistory.reduce((sum, item) => sum + (item.recordsAdded || 0), 0)
      };
      return stats;
    },

    filteredHistory() {
      let filtered = [...this.originalHistory];

      // Поиск по тексту
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        filtered = filtered.filter(item =>
            (item.fileName && item.fileName.toLowerCase().includes(query)) ||
            (item.user && item.user.toLowerCase().includes(query)) ||
            (this.getStatusText(item.status).toLowerCase().includes(query)) ||
            (item.errorMessage && item.errorMessage.toLowerCase().includes(query))
        );
      }

      // Фильтр по дате
      if (this.startDate) {
        const start = new Date(this.startDate);
        start.setHours(0, 0, 0, 0);
        filtered = filtered.filter(item => {
          const itemDate = new Date(item.importDate);
          return itemDate >= start;
        });
      }

      if (this.endDate) {
        const end = new Date(this.endDate);
        end.setHours(23, 59, 59, 999);
        filtered = filtered.filter(item => {
          const itemDate = new Date(item.importDate);
          return itemDate <= end;
        });
      }

      // Фильтр по статусу
      if (this.statusFilter) {
        filtered = filtered.filter(item => item.status === this.statusFilter);
      }

      // Фильтр по пользователю
      if (this.userFilter && this.userRole === 'ADMIN') {
        filtered = filtered.filter(item => item.user === this.userFilter);
      }

      // Сортировка
      filtered.sort((a, b) => {
        let aValue, bValue;

        switch (this.sortField) {
          case 'id':
            aValue = a.id;
            bValue = b.id;
            break;
          case 'fileName':
            aValue = a.fileName?.toLowerCase();
            bValue = b.fileName?.toLowerCase();
            break;
          case 'user':
            aValue = a.user?.toLowerCase();
            bValue = b.user?.toLowerCase();
            break;
          case 'status':
            aValue = a.status;
            bValue = b.status;
            break;
          case 'recordsAdded':
            aValue = a.recordsAdded || 0;
            bValue = b.recordsAdded || 0;
            break;
          case 'importDate':
          default:
            aValue = new Date(a.importDate);
            bValue = new Date(b.importDate);
        }

        if (this.sortDirection === 'asc') {
          return aValue > bValue ? 1 : -1;
        } else {
          return aValue < bValue ? 1 : -1;
        }
      });

      return filtered;
    },

    totalPages() {
      return Math.ceil(this.filteredHistory.length / this.pageSize);
    },

    startIndex() {
      return (this.currentPage - 1) * this.pageSize;
    },

    endIndex() {
      return Math.min(this.currentPage * this.pageSize, this.filteredHistory.length);
    },

    paginatedHistory() {
      return this.filteredHistory.slice(this.startIndex, this.endIndex);
    },

    visiblePages() {
      const pages = [];
      const maxVisible = 5;

      if (this.totalPages <= maxVisible) {
        for (let i = 1; i <= this.totalPages; i++) {
          pages.push(i);
        }
      } else {
        let start = Math.max(1, this.currentPage - 2);
        let end = Math.min(this.totalPages, start + maxVisible - 1);

        if (end - start + 1 < maxVisible) {
          start = end - maxVisible + 1;
        }

        for (let i = start; i <= end; i++) {
          pages.push(i);
        }
      }

      return pages;
    }
  },
  methods: {
    formatDate(date) {
      if (!date) return 'Н/Д';
      try {
        const d = new Date(date);
        return d.toLocaleDateString('ru-RU', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
      } catch (e) {
        return date;
      }
    },

    formatTimeAgo(date) {
      if (!date) return '';
      const now = new Date();
      const past = new Date(date);
      const diffMs = now - past;
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMs / 3600000);
      const diffDays = Math.floor(diffMs / 86400000);

      if (diffDays > 0) return `${diffDays} дн. назад`;
      if (diffHours > 0) return `${diffHours} ч. назад`;
      if (diffMins > 0) return `${diffMins} мин. назад`;
      return 'Только что';
    },

    formatFileSize(bytes) {
      if (!bytes || bytes === 0) return '0 Б';
      const k = 1024;
      const sizes = ['Б', 'КБ', 'МБ', 'ГБ'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },

    getStatusText(status) {
      const statusMap = {
        'SUCCESS': 'Успешно',
        'PROCESSING': 'В обработке',
        'FAILED': 'Ошибка',
        'PENDING': 'Ожидает'
      };
      return statusMap[status] || status;
    },

    truncateFileName(fileName, maxLength = 30) {
      if (!fileName) return 'Нет файла';
      if (fileName.length <= maxLength) return fileName;
      const extension = fileName.substring(fileName.lastIndexOf('.'));
      const name = fileName.substring(0, fileName.lastIndexOf('.'));
      const truncateLength = maxLength - extension.length - 3;
      return name.substring(0, truncateLength) + '...' + extension;
    },

    truncateFileKey(fileKey, maxLength = 40) {
      if (!fileKey) return '';
      if (fileKey.length <= maxLength) return fileKey;
      return fileKey.substring(0, maxLength) + '...';
    },

    viewDetails(item) {
      this.selectedItem = { ...item };
      // Добавляем флаг для отслеживания загрузки
      if (!this.selectedItem.downloading) {
        this.selectedItem.downloading = false;
      }
    },

    viewErrorDetails(item) {
      this.selectedItem = { ...item };
      // Прокручиваем к секции ошибок в модальном окне
      setTimeout(() => {
        const errorSection = document.querySelector('.error-details');
        if (errorSection) {
          errorSection.scrollIntoView({ behavior: 'smooth' });
        }
      }, 100);
    },

    closeModal() {
      this.selectedItem = null;
    },

    async downloadFile(item) {
      await this.performDownload(item);
    },

    async downloadFromModal(item) {
      await this.performDownload(item);
    },

    async performDownload(item) {
      const token = localStorage.getItem('authToken');
      if (!token) {
        this.showNotification('Требуется авторизация', 'error');
        this.$router.push('/login');
        return;
      }

      // Устанавливаем флаг загрузки
      item.downloading = true;

      try {
        // Попытка 1: Используем прямой endpoint для скачивания
        const downloadUrl = `/api/import/download/${item.id}`;

        const response = await fetch(downloadUrl, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });

        if (!response.ok) {
          // Если прямой endpoint не сработал, пробуем получить presigned URL
          await this.downloadViaPresignedUrl(item, token);
          return;
        }

        // Получаем оригинальное имя файла из заголовков или используем сохраненное
        let fileName = item.fileName;
        const contentDisposition = response.headers.get('Content-Disposition');
        if (contentDisposition) {
          const matches = contentDisposition.match(/filename="?([^"]+)"?/);
          if (matches && matches[1]) {
            fileName = matches[1];
          }
        }

        const blob = await response.blob();
        this.saveFile(blob, fileName);

        this.showNotification(`Файл "${this.truncateFileName(fileName)}" успешно скачан`, 'success');

      } catch (error) {
        console.error('Ошибка при скачивании файла:', error);
        this.showNotification('Не удалось скачать файл', 'error');

        // Пробуем альтернативный метод через пресигненый URL
        try {
          await this.downloadViaPresignedUrl(item, token);
        } catch (presignedError) {
          console.error('Ошибка при скачивании через presigned URL:', presignedError);
          this.showNotification('Файл недоступен для скачивания', 'error');
        }
      } finally {
        // Сбрасываем флаг загрузки
        item.downloading = false;
      }
    },

    async downloadViaPresignedUrl(item, token) {
      try {
        // Получаем presigned URL с сервера
        const response = await axios.get(`/api/import/file-url/${item.id}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.data && response.data.downloadUrl) {
          // Открываем URL в новом окне для скачивания
          window.open(response.data.downloadUrl, '_blank');
          this.showNotification('Скачивание началось в новом окне', 'info');
        } else {
          throw new Error('Не удалось получить ссылку для скачивания');
        }
      } catch (error) {
        console.error('Ошибка при получении presigned URL:', error);
        throw error;
      }
    },

    saveFile(blob, fileName) {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    },

    filterHistory() {
      this.currentPage = 1;
    },

    debouncedFilterHistory() {
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer);
      }
      this.debounceTimer = setTimeout(() => {
        this.filterHistory();
      }, 300);
    },

    clearFilters() {
      this.searchQuery = '';
      this.startDate = '';
      this.endDate = '';
      this.statusFilter = '';
      this.userFilter = '';
      this.currentPage = 1;
      this.showNotification('Фильтры очищены', 'info');
    },

    sortBy(field) {
      if (this.sortField === field) {
        this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
      } else {
        this.sortField = field;
        this.sortDirection = 'desc';
      }
    },

    goToPage(page) {
      this.currentPage = page;
      // Прокручиваем к началу таблицы
      const table = document.querySelector('.history-table-container');
      if (table) {
        table.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    },

    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
        this.goToPage(this.currentPage);
      }
    },

    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
        this.goToPage(this.currentPage);
      }
    },

    resetPagination() {
      this.currentPage = 1;
    },

    async refreshHistory() {
      await this.loadHistory();
      this.showNotification('История импорта обновлена', 'success');
    },

    showNotification(message, type = 'info') {
      this.notification = { message, type };
      setTimeout(() => {
        this.clearNotification();
      }, 5000);
    },

    clearNotification() {
      this.notification = { message: '', type: 'info' };
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
        const response = await axios.get('/api/import/history', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        console.log('Ответ от сервера:', response.data);

        if (response.data && response.data.history) {
          // Добавляем флаг downloading к каждому элементу
          this.importHistory = response.data.history.map(item => ({
            ...item,
            downloading: false
          }));
          this.originalHistory = [...this.importHistory];

          // Получаем роль пользователя из первого элемента
          if (this.importHistory.length > 0) {
            const userInfo = await this.getUserInfo(token);
            this.userRole = userInfo.role || 'USER';
          }
        } else {
          this.importHistory = [];
          this.originalHistory = [];
          console.warn('История импорта пуста или не в ожидаемом формате');
        }

      } catch (error) {
        console.error('Ошибка загрузки истории:', error);

        if (error.response) {
          console.error('Статус ошибки:', error.response.status);
          console.error('Данные ошибки:', error.response.data);

          if (error.response.status === 401) {
            this.showNotification('Сессия истекла. Пожалуйста, войдите снова.', 'error');
            setTimeout(() => {
              this.$router.push('/login');
            }, 2000);
          } else if (error.response.status === 403) {
            this.showNotification('У вас нет прав для просмотра истории импорта.', 'error');
          } else {
            this.showNotification(`Ошибка загрузки истории: ${error.response.data.error || error.response.status}`, 'error');
          }
        } else if (error.request) {
          console.error('Запрос был сделан, но ответ не получен:', error.request);
          this.showNotification('Ошибка сети. Проверьте соединение с сервером.', 'error');
        } else {
          this.showNotification('Произошла ошибка при загрузке истории.', 'error');
        }

        this.importHistory = [];
        this.originalHistory = [];
      } finally {
        this.loading = false;
      }
    },

    async getUserInfo(token) {
      try {
        // Эндпоинт для получения информации о пользователе
        const response = await axios.get('/api/auth/me', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        return response.data;
      } catch (error) {
        console.error('Ошибка получения информации о пользователе:', error);
        return { role: 'USER' };
      }
    }
  },
  mounted() {
    this.loadHistory();

    // Устанавливаем дату "с" как 30 дней назад по умолчанию
    const defaultStartDate = new Date();
    defaultStartDate.setDate(defaultStartDate.getDate() - 30);
    this.startDate = defaultStartDate.toISOString().split('T')[0];

    // Устанавливаем дату "по" как сегодня
    const today = new Date();
    this.endDate = today.toISOString().split('T')[0];

    console.log('Текущий URL API:', window.location.origin + '/api/import/history');

    // Слушаем событие обновления истории при импорте
    this.$root.$on('importCompleted', () => {
      setTimeout(() => {
        this.loadHistory();
      }, 1000);
    });
  },

  beforeDestroy() {
    // Удаляем слушатель события
    this.$root.$off('importCompleted');
  }
}
</script>

<style scoped>
.import-history {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  background: #f8fafc;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 12px;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.refresh-btn:hover:not(:disabled) {
  background: #5a67d8;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.filters-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.filters-row {
  display: grid;
  grid-template-columns: 1fr auto auto auto;
  gap: 16px;
  margin-bottom: 16px;
  align-items: center;
}

.search-filter {
  grid-column: 1;
}

.input-with-icon {
  position: relative;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
  background: #f8fafc;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
}

.date-filters {
  display: flex;
  gap: 12px;
  align-items: center;
}

.date-filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-label {
  font-size: 13px;
  color: #4a5568;
  font-weight: 500;
  white-space: nowrap;
}

.date-input {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  min-width: 120px;
}

.date-input:focus {
  outline: none;
  border-color: #667eea;
}

.status-filters,
.user-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 13px;
  color: #4a5568;
  font-weight: 500;
  white-space: nowrap;
}

.status-select,
.user-select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  min-width: 140px;
  background: white;
  cursor: pointer;
}

.filter-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
  font-size: 13px;
  color: #718096;
}

.clear-filters-btn {
  padding: 6px 12px;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-filters-btn:hover {
  background: #cbd5e0;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 4px;
}

.stat-value.stat-success {
  color: #38a169;
}

.stat-value.stat-failed {
  color: #e53e3e;
}

.stat-label {
  font-size: 14px;
  color: #718096;
}

.history-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.table-wrapper {
  overflow-x: auto;
}

.table-header {
  display: grid;
  grid-template-columns: 80px 180px minmax(200px, 1fr) 120px 120px 100px 140px;
  background: #667eea;
  color: white;
  font-weight: 600;
  font-size: 14px;
  min-width: 1000px;
}

.header-cell {
  padding: 16px;
  text-align: left;
  cursor: pointer;
  user-select: none;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s ease;
}

.header-cell:hover {
  background: #5a67d8;
}

.sort-icon {
  font-size: 12px;
  opacity: 0.8;
}

.actions-header {
  cursor: default;
}

.actions-header:hover {
  background: #667eea;
}

.table-body {
  min-height: 400px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #718096;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e2e8f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #718096;
  text-align: center;
  padding: 40px;
}

.empty-icon {
  margin-bottom: 20px;
  opacity: 0.5;
}

.history-items {
  min-width: 1000px;
}

.history-item {
  display: grid;
  grid-template-columns: 80px 180px minmax(200px, 1fr) 120px 120px 100px 140px;
  border-bottom: 1px solid #e2e8f0;
  transition: background 0.2s ease;
  min-height: 70px;
}

.history-item:hover {
  background: #f8fafc;
}

.history-item.item-success {
  border-left: 4px solid #38a169;
}

.history-item.item-failed {
  border-left: 4px solid #e53e3e;
}

.history-item.item-processing {
  border-left: 4px solid #ed8936;
}

.cell {
  padding: 16px;
  display: flex;
  align-items: center;
  color: #4a5568;
  font-size: 14px;
}

.cell-id {
  font-family: 'Monospace', 'Courier New', monospace;
  font-weight: 600;
  color: #2d3748;
}

.cell-date {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.date-time {
  font-size: 13px;
  color: #2d3748;
}

.time-ago {
  font-size: 12px;
  color: #a0aec0;
}

.cell-file {
  .file-info {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;
  }

  .file-details {
    flex: 1;
    min-width: 0; /* Для обрезки текста */
  }

  .file-name {
    font-weight: 500;
    color: #2d3748;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .file-meta {
    font-size: 12px;
    color: #a0aec0;
    margin-top: 2px;
  }
}

.cell-user {
  .user-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 8px;
    background: #edf2f7;
    border-radius: 6px;
    font-size: 13px;
  }
}

.cell-status {
  flex-direction: column;
  gap: 6px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  text-transform: uppercase;
}

.status-success {
  background: #c6f6d5;
  color: #276749;
}

.status-failed {
  background: #fed7d7;
  color: #c53030;
}

.status-processing {
  background: #feebc8;
  color: #c05621;
}

.processing-dots {
  display: inline-flex;
  gap: 2px;
}

.dot {
  animation: blink 1.4s infinite both;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {
  0%, 100% { opacity: 0.2; }
  50% { opacity: 1; }
}

.error-hint {
  font-size: 11px;
  color: #e53e3e;
  background: #fff5f5;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #fed7d7;
}

.cell-records {
  .records-count {
    font-size: 18px;
    font-weight: 600;
    color: #2d3748;
  }

  .zero-records {
    color: #a0aec0;
  }
}

.cell-actions {
  .action-buttons {
    display: flex;
    gap: 8px;
  }

  .action-btn {
    width: 36px;
    height: 36px;
    border: none;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
  }

  .view-btn {
    background: #e2e8f0;
    color: #4a5568;
  }

  .view-btn:hover {
    background: #cbd5e0;
    transform: translateY(-1px);
  }

  .download-btn {
    background: #38a169;
    color: white;
  }

  .download-btn:hover:not(:disabled) {
    background: #2f855a;
    transform: translateY(-1px);
  }

  .download-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .error-btn {
    background: #fed7d7;
    color: #c53030;
  }

  .error-btn:hover {
    background: #feb2b2;
    transform: translateY(-1px);
  }
}

.download-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
}

.pagination-info {
  font-size: 14px;
  color: #718096;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  align-items: center;
  gap: 4px;
}

.page-btn {
  min-width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
}

.page-btn:hover {
  border-color: #667eea;
}

.page-btn.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #718096;
}

.page-size-selector select {
  padding: 6px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

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
  backdrop-filter: blur(4px);
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  animation: modalSlideIn 0.3s ease-out;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 20px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #a0aec0;
  cursor: pointer;
  padding: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f7fafc;
  color: #4a5568;
}

.modal-body {
  padding: 24px;
}

.detail-section {
  margin-bottom: 32px;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e2e8f0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.detail-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 13px;
  color: #718096;
  font-weight: 500;
}

.detail-value {
  font-size: 15px;
  color: #2d3748;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-value {
  align-items: flex-start;
}

.file-icon-small,
.user-icon-small {
  flex-shrink: 0;
}

.file-size-small {
  font-size: 13px;
  color: #a0aec0;
  font-weight: normal;
}

.user-id {
  font-size: 13px;
  color: #a0aec0;
  font-weight: normal;
}

.status-badge-large {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  display: inline-block;
}

.records-value {
  font-size: 18px;
  font-weight: 700;
}

.error-details {
  background: #fff5f5;
  border: 1px solid #fed7d7;
  border-radius: 8px;
  padding: 16px;
}

.error-message {
  color: #c53030;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.error-time {
  margin-top: 8px;
  font-size: 13px;
  color: #e53e3e;
}

.file-actions {
  background: #f8fafc;
  border-radius: 8px;
  padding: 20px;
}

.download-file-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: #38a169;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 16px;
  width: 100%;
  justify-content: center;
}

.download-file-btn:hover:not(:disabled) {
  background: #2f855a;
  transform: translateY(-1px);
}

.download-file-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.file-info-details {
  display: grid;
  gap: 12px;
}

.file-info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-label {
  font-size: 13px;
  color: #718096;
  min-width: 80px;
}

.info-value {
  font-size: 14px;
  color: #2d3748;
  font-weight: 500;
}

.file-key {
  font-family: 'Monospace', 'Courier New', monospace;
  background: #edf2f7;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.transaction-details {
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.tx-label {
  font-size: 13px;
  color: #718096;
  min-width: 140px;
}

.tx-value {
  font-size: 14px;
  color: #2d3748;
  font-weight: 500;
}

.tx-success {
  color: #38a169;
}

.tx-failed {
  color: #e53e3e;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid #e2e8f0;
}

.modal-close-btn {
  padding: 10px 20px;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-close-btn:hover {
  background: #cbd5e0;
}

.modal-download-btn {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.modal-download-btn:hover:not(:disabled) {
  background: #5a67d8;
}

.modal-download-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-download-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.notification {
  position: fixed;
  bottom: 24px;
  right: 24px;
  padding: 16px 24px;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  max-width: 400px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  z-index: 1001;
  animation: slideInRight 0.3s ease-out;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.notification.info {
  background: #667eea;
}

.notification.success {
  background: #38a169;
}

.notification.error {
  background: #e53e3e;
}

.notification-close {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.8;
  transition: opacity 0.2s ease;
}

.notification-close:hover {
  opacity: 1;
}

/* Адаптивность */
@media (max-width: 1200px) {
  .filters-row {
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }

  .search-filter {
    grid-column: 1 / span 2;
  }
}

@media (max-width: 768px) {
  .import-history {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .filters-row {
    grid-template-columns: 1fr;
  }

  .search-filter {
    grid-column: 1;
  }

  .date-filters {
    flex-direction: column;
    align-items: stretch;
  }

  .date-filter-group {
    flex-direction: column;
    align-items: flex-start;
  }

  .date-input {
    width: 100%;
  }

  .status-filters,
  .user-filter {
    flex-direction: column;
    align-items: flex-start;
  }

  .status-select,
  .user-select {
    width: 100%;
  }

  .table-header,
  .history-item {
    grid-template-columns: 60px 140px minmax(150px, 1fr) 100px 80px 60px;
  }

  .cell-user {
    display: none;
  }

  .stats-section {
    grid-template-columns: 1fr 1fr;
  }

  .modal-content {
    width: 95%;
    margin: 10px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .pagination {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .pagination-controls {
    order: 2;
    justify-content: center;
  }

  .page-size-selector {
    order: 3;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .header-actions {
    width: 100%;
  }

  .refresh-btn {
    width: 100%;
    justify-content: center;
  }

  .table-header,
  .history-item {
    grid-template-columns: 50px 120px 1fr 80px 50px;
    min-width: 0;
  }

  .cell-status,
  .cell-records {
    font-size: 12px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }

  .action-btn {
    width: 32px;
    height: 32px;
  }

  .modal-footer {
    flex-direction: column;
  }

  .modal-close-btn,
  .modal-download-btn {
    width: 100%;
  }
}
</style>