<template>
  <div class="special-operations">
    <div class="header">
      <h1>Специальные операции с организациями</h1>
    </div>

    <div class="operations-grid">
      <div class="operation-card">
        <h3>Средний рейтинг организаций</h3>
        <p>Рассчитать среднее значение поля rating для всех объектов</p>
        <button class="operation-btn" @click="calculateAverageRating" :disabled="loadingAvgRating">
          {{ loadingAvgRating ? 'Вычисление...' : 'Рассчитать' }}
        </button>
        <div v-if="averageRating !== null" class="result">
          <strong>Средний рейтинг: {{ averageRating.toFixed(2) }}</strong>
        </div>
      </div>

      <div class="operation-card">
        <h3>Поиск по названию</h3>
        <p>Вернуть массив объектов, значение поля name которых начинается с заданной подстроки</p>
        <div class="input-group">
          <input
            v-model="nameSearch"
            type="text"
            placeholder="Введите начало названия..."
            class="search-input"
          >
          <button class="operation-btn" @click="searchByName" :disabled="loadingNameSearch">
            {{ loadingNameSearch ? 'Поиск...' : 'Найти' }}
          </button>
        </div>
        <div v-if="nameSearchResults.length > 0" class="result">
          <h4>Найдено организаций: {{ nameSearchResults.length }}</h4>
          <ul class="results-list">
            <li v-for="org in nameSearchResults" :key="org.id" class="result-item">
              {{ org.name }} (ID: {{ org.id }}, Рейтинг: {{ org.rating }})
            </li>
          </ul>
        </div>
        <div v-else-if="nameSearchPerformed && !loadingNameSearch" class="no-results">
          Организации не найдены
        </div>
      </div>

      <div class="operation-card">
        <h3>Фильтр по почтовому адресу</h3>
        <p>Вернуть массив объектов, значение поля postalAddress которых больше заданного</p>
        <div class="input-group">
          <input
            v-model.number="addressFilter"
            type="number"
            placeholder="Введите минимальный ID адреса..."
            class="search-input"
            min="0"
          >
          <button class="operation-btn" @click="filterByAddress" :disabled="loadingAddressFilter">
            {{ loadingAddressFilter ? 'Фильтрация...' : 'Фильтровать' }}
          </button>
        </div>
        <div v-if="addressFilterResults.length > 0" class="result">
          <h4>Найдено организаций: {{ addressFilterResults.length }}</h4>
          <ul class="results-list">
            <li v-for="org in addressFilterResults" :key="org.id" class="result-item">
              {{ org.name }} (Адрес ID: {{ org.postalAddress }}, ID организации: {{ org.id }})
            </li>
          </ul>
        </div>
        <div v-else-if="addressFilterPerformed && !loadingAddressFilter" class="no-results">
          Организации не найдены
        </div>
      </div>

      <div class="operation-card wide-card">
        <h3>Объединение организаций</h3>
        <p>Объединить организации, создав новую и зачислив в неё всех сотрудников двух исходных</p>

        <div class="form-grid">
          <div class="form-group">
            <label>Первая организация (ID):</label>
            <input v-model.number="mergeData.firstOrgId" type="number" class="form-input" min="1">
          </div>
          <div class="form-group">
            <label>Вторая организация (ID):</label>
            <input v-model.number="mergeData.secondOrgId" type="number" class="form-input" min="1">
          </div>
          <div class="form-group">
            <label>Название новой организации:</label>
            <input v-model="mergeData.newName" type="text" class="form-input" placeholder="Введите название">
          </div>
          <div class="form-group">
            <label>Адрес новой организации (ID):</label>
            <input v-model.number="mergeData.newAddressId" type="number" class="form-input" min="1">
          </div>
        </div>

        <button class="operation-btn merge-btn" @click="mergeOrganizations" :disabled="loadingMerge">
          {{ loadingMerge ? 'Объединение...' : 'Объединить организации' }}
        </button>

        <div v-if="mergeResult" class="result">
          <strong>{{ mergeResult }}</strong>
        </div>
      </div>

      <div class="operation-card wide-card">
        <h3>Поглощение организации</h3>
        <p>Поглотить одну организацию другой без увольнения сотрудников</p>

        <div class="form-grid">
          <div class="form-group">
            <label>Поглощающая организация (ID):</label>
            <input v-model.number="absorbData.absorbingOrgId" type="number" class="form-input" min="1">
          </div>
          <div class="form-group">
            <label>Поглощаемая организация (ID):</label>
            <input v-model.number="absorbData.absorbedOrgId" type="number" class="form-input" min="1">
          </div>
        </div>

        <button class="operation-btn absorb-btn" @click="absorbOrganization" :disabled="loadingAbsorb">
          {{ loadingAbsorb ? 'Поглощение...' : 'Поглотить организацию' }}
        </button>

        <div v-if="absorbResult" class="result">
          <strong>{{ absorbResult }}</strong>
        </div>
      </div>
    </div>

    <div v-if="notification.show" class="notification" :class="notification.type">
      {{ notification.message }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'SpecialOperations',
  data() {
    return {
      averageRating: null,
      loadingAvgRating: false,

      nameSearch: '',
      nameSearchResults: [],
      loadingNameSearch: false,
      nameSearchPerformed: false,

      addressFilter: '',
      addressFilterResults: [],
      loadingAddressFilter: false,
      addressFilterPerformed: false,

      mergeData: {
        firstOrgId: '',
        secondOrgId: '',
        newName: '',
        newAddressId: ''
      },
      mergeResult: '',
      loadingMerge: false,

      absorbData: {
        absorbingOrgId: '',
        absorbedOrgId: ''
      },
      absorbResult: '',
      loadingAbsorb: false,

      notification: {
        show: false,
        message: '',
        type: 'success'
      }
    }
  },
  methods: {
    async calculateAverageRating() {
      this.loadingAvgRating = true
      try {
        const response = await this.$axios.post('/api/special/avg-rating')
        this.averageRating = response.data.averageRating
        this.showNotification(response.data.message || 'Средний рейтинг успешно рассчитан', 'success')
      } catch (error) {
        const errorMessage = error.response?.data?.error || 'Ошибка при расчете среднего рейтинга'
        this.showNotification(errorMessage, 'error')
        this.averageRating = null
      } finally {
        this.loadingAvgRating = false
      }
    },

    async searchByName() {
      if (!this.nameSearch.trim()) {
        this.showNotification('Введите подстроку для поиска', 'error')
        return
      }

      this.loadingNameSearch = true
      this.nameSearchPerformed = true
      try {
        const response = await this.$axios.post('/api/special/search-by-name', {
          substring: this.nameSearch
        })
        this.nameSearchResults = response.data.organizations || []
        const count = response.data.count || this.nameSearchResults.length
        this.showNotification(response.data.message || `Найдено ${count} организаций`, 'success')
      } catch (error) {
        const errorMessage = error.response?.data?.error || 'Ошибка при поиске по имени'
        this.showNotification(errorMessage, 'error')
        this.nameSearchResults = []
      } finally {
        this.loadingNameSearch = false
      }
    },

    async filterByAddress() {
      if (!this.addressFilter || this.addressFilter < 0) {
        this.showNotification('Введите корректное значение для фильтрации (положительное число)', 'error')
        return
      }

      this.loadingAddressFilter = true
      this.addressFilterPerformed = true
      try {
        const response = await this.$axios.post('/api/special/filter-by-address', {
          minAddressId: this.addressFilter
        })
        this.addressFilterResults = response.data.organizations || []
        const count = response.data.count || this.addressFilterResults.length
        this.showNotification(response.data.message || `Найдено ${count} организаций`, 'success')
      } catch (error) {
        const errorMessage = error.response?.data?.error || 'Ошибка при фильтрации по адресу'
        this.showNotification(errorMessage, 'error')
        this.addressFilterResults = []
      } finally {
        this.loadingAddressFilter = false
      }
    },

    async mergeOrganizations() {
      if (!this.mergeData.firstOrgId || !this.mergeData.secondOrgId ||
          !this.mergeData.newName?.trim() || !this.mergeData.newAddressId) {
        this.showNotification('Заполните все поля для объединения', 'error')
        return
      }

      if (this.mergeData.firstOrgId === this.mergeData.secondOrgId) {
        this.showNotification('Нельзя объединить организацию саму с собой', 'error')
        return
      }

      this.loadingMerge = true
      try {
        const response = await this.$axios.post('/api/special/merge-organizations', this.mergeData)
        this.mergeResult = response.data.message
        this.showNotification('Организации успешно объединены', 'success')

        this.mergeData = {
          firstOrgId: '',
          secondOrgId: '',
          newName: '',
          newAddressId: ''
        }

        this.nameSearchResults = []
        this.addressFilterResults = []
        this.nameSearchPerformed = false
        this.addressFilterPerformed = false
      } catch (error) {
        const errorMessage = error.response?.data?.error || 'Ошибка при объединении организаций'
        this.mergeResult = errorMessage
        this.showNotification(errorMessage, 'error')
      } finally {
        this.loadingMerge = false
      }
    },

    async absorbOrganization() {
      if (!this.absorbData.absorbingOrgId || !this.absorbData.absorbedOrgId) {
        this.showNotification('Заполните ID обеих организаций', 'error')
        return
      }

      if (this.absorbData.absorbingOrgId === this.absorbData.absorbedOrgId) {
        this.showNotification('Нельзя поглотить организацию саму себя', 'error')
        return
      }

      this.loadingAbsorb = true
      try {
        const response = await this.$axios.post('/api/special/absorb-organization', this.absorbData)
        this.absorbResult = response.data.message
        this.showNotification('Организация успешно поглощена', 'success')

        this.absorbData = {
          absorbingOrgId: '',
          absorbedOrgId: ''
        }

        this.nameSearchResults = []
        this.addressFilterResults = []
        this.nameSearchPerformed = false
        this.addressFilterPerformed = false
      } catch (error) {
        const errorMessage = error.response?.data?.error || 'Ошибка при поглощении организации'
        this.absorbResult = errorMessage
        this.showNotification(errorMessage, 'error')
      } finally {
        this.loadingAbsorb = false
      }
    },

    showNotification(message, type = 'success') {
      this.notification = {
        show: true,
        message,
        type
      }
      setTimeout(() => {
        this.notification.show = false
      }, 5000)
    }
  }
}
</script>

<style scoped>
.special-operations {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  color: #2c3e50;
  font-size: 2.5rem;
  margin-bottom: 10px;
}

.operations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}

.operation-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 1px solid #e1e5e9;
  transition: all 0.3s ease;
}

.operation-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.wide-card {
  grid-column: span 2;
}

.operation-card h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 1.4rem;
  border-bottom: 2px solid #667eea;
  padding-bottom: 8px;
}

.operation-card p {
  color: #6c757d;
  margin-bottom: 20px;
  line-height: 1.5;
}

.input-group {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.search-input, .form-input {
  flex: 1;
  padding: 12px 15px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s ease;
}

.search-input:focus, .form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 5px;
  font-weight: 600;
  color: #495057;
}

.operation-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  width: 100%;
  margin-bottom: 15px;
}

.operation-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
}

.operation-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
  transform: none;
}

.merge-btn {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
}

.absorb-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
}

.result {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #28a745;
  margin-top: 15px;
}

.no-results {
  background: #fff3cd;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid #ffc107;
  margin-top: 15px;
  color: #856404;
  text-align: center;
}

.results-list {
  list-style: none;
  padding: 0;
  margin: 10px 0 0 0;
  max-height: 200px;
  overflow-y: auto;
}

.result-item {
  padding: 8px 12px;
  border-bottom: 1px solid #e9ecef;
  transition: background-color 0.2s ease;
}

.result-item:hover {
  background-color: #e9ecef;
}

.result-item:last-child {
  border-bottom: none;
}

.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 16px 20px;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  z-index: 1001;
  animation: slideIn 0.3s ease-out;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  max-width: 400px;
}

.notification.success {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
}

.notification.error {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Адаптивность */
@media (max-width: 1200px) {
  .operations-grid {
    grid-template-columns: 1fr;
  }

  .wide-card {
    grid-column: span 1;
  }
}

@media (max-width: 768px) {
  .special-operations {
    padding: 10px;
  }

  .operations-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .operation-card {
    padding: 20px;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .input-group {
    flex-direction: column;
  }

  .header h1 {
    font-size: 2rem;
  }
}

@media (max-width: 480px) {
  .operation-card {
    padding: 15px;
  }

  .notification {
    right: 10px;
    left: 10px;
    max-width: none;
  }
}
</style>
