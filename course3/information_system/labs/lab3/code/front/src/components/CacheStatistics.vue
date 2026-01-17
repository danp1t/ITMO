<template>
  <div class="cache-statistics">
    <h2>Управление статистикой кэша</h2>

    <div class="status-panel">
      <div class="status-card">
        <h3>Текущий статус</h3>
        <div class="status-indicator" :class="{ 'enabled': status.enabled, 'disabled': !status.enabled }">
          {{ status.enabled ? 'ВКЛЮЧЕНА' : 'ВЫКЛЮЧЕНА' }}
        </div>
        <div class="summary" v-if="status.summary">
          <h4>Сводка:</h4>
          <pre>{{ status.summary }}</pre>
        </div>
      </div>
    </div>

    <div class="controls">
      <h3>Управление</h3>
      <div class="buttons-grid">
        <button
            @click="enableStatistics"
            :disabled="status.enabled"
            class="btn-enable"
        >
          <svg class="btn-icon" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
          </svg>
          Включить статистику
        </button>

        <button
            @click="disableStatistics"
            :disabled="!status.enabled"
            class="btn-disable"
        >
          <svg class="btn-icon" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z"/>
          </svg>
          Выключить статистику
        </button>

        <button
            @click="resetStatistics"
            :disabled="!status.enabled"
            class="btn-reset"
        >
          <svg class="btn-icon" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
          </svg>
          Сбросить статистику
        </button>

        <button
            @click="refreshStatus"
            class="btn-refresh"
        >
          <svg class="btn-icon" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
          </svg>
          Обновить статус
        </button>
      </div>
    </div>

    <div v-if="message" class="message" :class="{ 'success': messageType === 'success', 'error': messageType === 'error' }">
      {{ message }}
    </div>

    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <span>Загрузка...</span>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CacheStatistics',
  data() {
    return {
      status: {
        enabled: false,
        summary: ''
      },
      loading: false,
      message: '',
      messageType: ''
    }
  },
  mounted() {
    this.loadStatus();
  },
  methods: {
    async loadStatus() {
      const token = localStorage.getItem('authToken');
      if (!token) {
        this.showMessage('Требуется авторизация', 'error');
        return;
      }

      this.loading = true;
      try {
        const response = await axios.get('/api/cache-statistics/status', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        this.status = response.data;
        this.showMessage('Статус загружен', 'success');
      } catch (error) {
        console.error('Ошибка загрузки статуса:', error);
        this.showMessage('Ошибка загрузки статуса', 'error');
      } finally {
        this.loading = false;
      }
    },

    async enableStatistics() {

      this.loading = true;
      try {
        const token = localStorage.getItem('authToken');
        const response = await axios.post('/api/cache-statistics/enable', {}, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        this.status.enabled = true;
        this.showMessage(response.data, 'success');
        await this.loadStatus(); // Обновляем полный статус
      } catch (error) {
        console.error('Ошибка включения статистики:', error);
        this.showMessage('Ошибка включения статистики', 'error');
      } finally {
        this.loading = false;
      }
    },

    async disableStatistics() {

      this.loading = true;
      try {
        const token = localStorage.getItem('authToken');
        const response = await axios.post('/api/cache-statistics/disable', {}, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        this.status.enabled = false;
        this.showMessage(response.data, 'success');
        await this.loadStatus(); // Обновляем полный статус
      } catch (error) {
        console.error('Ошибка выключения статистики:', error);
        this.showMessage('Ошибка выключения статистики', 'error');
      } finally {
        this.loading = false;
      }
    },

    async resetStatistics() {
      this.loading = true;
      try {
        const token = localStorage.getItem('authToken');
        const response = await axios.post('/api/cache-statistics/reset', {}, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        this.showMessage(response.data, 'success');
        await this.loadStatus(); // Обновляем статус после сброса
      } catch (error) {
        console.error('Ошибка сброса статистики:', error);
        this.showMessage('Ошибка сброса статистики', 'error');
      } finally {
        this.loading = false;
      }
    },

    refreshStatus() {
      this.loadStatus();
    },

    showMessage(text, type) {
      this.message = text;
      this.messageType = type;
      setTimeout(() => {
        this.message = '';
        this.messageType = '';
      }, 3000);
    },
  }
}
</script>

<style scoped>
.cache-statistics {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 40px;
  font-size: 28px;
}

.status-panel {
  margin-bottom: 40px;
}

.status-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.status-card h3 {
  color: #4a5568;
  margin-bottom: 20px;
  font-size: 20px;
}

.status-indicator {
  display: inline-block;
  padding: 8px 20px;
  border-radius: 20px;
  font-weight: 700;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 20px;
}

.status-indicator.enabled {
  background: #c6f6d5;
  color: #276749;
}

.status-indicator.disabled {
  background: #fed7d7;
  color: #c53030;
}

.summary {
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
  margin-top: 15px;
}

.summary h4 {
  color: #4a5568;
  margin-bottom: 10px;
  font-size: 16px;
}

.summary pre {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #2d3748;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
}

.controls {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.controls h3 {
  color: #4a5568;
  margin-bottom: 25px;
  font-size: 20px;
}

.buttons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 15px 20px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-enable {
  background: #48bb78;
  color: white;
}

.btn-enable:hover:not(:disabled) {
  background: #38a169;
  transform: translateY(-2px);
}

.btn-disable {
  background: #f56565;
  color: white;
}

.btn-disable:hover:not(:disabled) {
  background: #e53e3e;
  transform: translateY(-2px);
}

.btn-reset {
  background: #ed8936;
  color: white;
}

.btn-reset:hover:not(:disabled) {
  background: #dd6b20;
  transform: translateY(-2px);
}

.btn-refresh {
  background: #4299e1;
  color: white;
}

.btn-refresh:hover:not(:disabled) {
  background: #3182ce;
  transform: translateY(-2px);
}

.btn-icon {
  flex-shrink: 0;
}

.message {
  margin-top: 25px;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  font-weight: 500;
  animation: fadeIn 0.3s ease;
}

.message.success {
  background: #c6f6d5;
  color: #276749;
  border: 1px solid #9ae6b4;
}

.message.error {
  background: #fed7d7;
  color: #c53030;
  border: 1px solid #fc8181;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .cache-statistics {
    padding: 20px;
  }

  .buttons-grid {
    grid-template-columns: 1fr;
  }

  .status-card,
  .controls {
    padding: 20px;
  }
}
</style>