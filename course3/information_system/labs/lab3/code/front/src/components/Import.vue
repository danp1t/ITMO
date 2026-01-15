<template>
  <div class="mass-import">
    <h2>Массовый импорт организаций</h2>

    <div class="upload-area"
         @dragover.prevent="dragover = true"
         @dragleave="dragover = false"
         @drop.prevent="handleDrop">
      <input
          type="file"
          ref="fileInput"
          @change="handleFileSelect"
          accept=".xml"
          style="display: none"
      />

      <div v-if="!selectedFile"
           class="upload-placeholder"
           :class="{ 'drag-over': dragover }"
           @click="$refs.fileInput.click()">
        <svg class="upload-icon" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#667eea" stroke-width="2">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
          <polyline points="17 8 12 3 7 8"/>
          <line x1="12" y1="3" x2="12" y2="15"/>
        </svg>
        <p>Нажмите для выбора файла или перетащите XML-файл сюда</p>
        <p class="format-hint">Поддерживается только формат XML</p>
      </div>

      <div v-else class="file-selected">
        <div class="file-info">
          <svg class="file-icon" width="48" height="48" viewBox="0 0 24 24" fill="#667eea">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
          <div>
            <p class="file-name">{{ selectedFile.name }}</p>
            <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
          </div>
          <button @click="removeFile" class="remove-btn">×</button>
        </div>

        <div class="actions">
          <button @click="uploadFile" :disabled="uploading" class="upload-btn">
            <span v-if="uploading">Загрузка...</span>
            <span v-else>Загрузить файл</span>
          </button>
          <button @click="selectAnother" class="secondary-btn">Выбрать другой файл</button>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>

    <div class="format-info">
      <h3>Требования к файлу:</h3>
      <ul>
        <li>Файл должен быть в формате XML</li>
        <li>Максимальный размер файла: 10MB</li>
        <li>Структура XML должна соответствовать шаблону</li>
      </ul>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: 'MassImport',
  data() {
    return {
      selectedFile: null,
      dragover: false,
      uploading: false,
      errorMessage: '',
      successMessage: ''
    }
  },
  methods: {
    handleFileSelect(event) {
      const file = event.target.files[0]
      this.validateAndSetFile(file)
    },

    handleDrop(event) {
      this.dragover = false
      const file = event.dataTransfer.files[0]
      this.validateAndSetFile(file)
    },

    validateAndSetFile(file) {
      this.errorMessage = ''
      this.successMessage = ''

      if (!file) return

      const fileName = file.name.toLowerCase()
      if (!fileName.endsWith('.xml')) {
        this.errorMessage = 'Ошибка: поддерживается только формат XML. Выберите файл с расширением .xml'
        return
      }

      const maxSize = 10 * 1024 * 1024
      if (file.size > maxSize) {
        this.errorMessage = `Ошибка: размер файла превышает 10MB. Ваш файл: ${this.formatFileSize(file.size)}`
        return
      }

      this.selectedFile = file
    },


    removeFile() {
      this.selectedFile = null
      this.errorMessage = ''
      this.successMessage = ''
      this.$refs.fileInput.value = ''
    },

    selectAnother() {
      this.$refs.fileInput.click()
    },

    async uploadFile() {
      if (!this.selectedFile) {
        this.errorMessage = 'Пожалуйста, выберите файл для загрузки'
        return
      }

      const token = localStorage.getItem('authToken');
      if (!token) {
        this.errorMessage = 'Требуется авторизация. Пожалуйста, войдите в систему.'
        return;
      }

      this.uploading = true
      this.errorMessage = ''
      this.successMessage = ''

      try {
        const formData = new FormData();
        formData.append('file', this.selectedFile);

        const token = localStorage.getItem('authToken');
        if (!token) {
          this.errorMessage = 'Требуется авторизация. Пожалуйста, войдите в систему.';
          return;
        }

        const response = await axios.post('/api/import/xml', formData, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'multipart/form-data'
          }}
        )

        if (response.data) {
          this.successMessage = `Импорт успешно завершен! Добавлено записей: ${response.data.recordsAdded}`;

          this.$emit('importCompleted', response.data.operationId);

          setTimeout(() => {
            this.selectedFile = null;
            this.successMessage = '';
            this.$refs.fileInput.value = '';
          }, 5000);
        }

      } catch (error) {
        if (error.response && error.response.data && error.response.data.error) {
          this.errorMessage = error.response.data.error;
        } else {
          this.errorMessage = 'Ошибка при импорте файла. Пожалуйста, попробуйте снова.';
        }
        console.error('Upload error:', error);
      } finally {
        this.uploading = false;
      }
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
  }
}
</script>

<style scoped>
.mass-import {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}

.upload-area {
  border: 2px dashed #667eea;
  border-radius: 12px;
  padding: 40px 20px;
  margin-bottom: 30px;
  background: #f8faff;
  transition: all 0.3s ease;
}

.upload-area.drag-over {
  background: #eef2ff;
  border-color: #4c51bf;
}

.upload-placeholder {
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.upload-placeholder:hover {
  transform: scale(1.02);
}

.upload-icon {
  margin-bottom: 20px;
}

.format-hint {
  color: #667eea;
  font-weight: 600;
  margin-top: 10px;
}

.file-selected {
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.file-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.file-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.file-size {
  color: #666;
  font-size: 14px;
}

.remove-btn {
  background: #fc8181;
  color: white;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s ease;
}

.remove-btn:hover {
  background: #f56565;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.upload-btn {
  padding: 12px 30px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-btn:hover:not(:disabled) {
  background: #5a67d8;
  transform: translateY(-2px);
}

.upload-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.secondary-btn {
  padding: 12px 30px;
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.secondary-btn:hover {
  background: #f8faff;
}

.error-message {
  background: #fed7d7;
  color: #c53030;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #f56565;
}

.success-message {
  background: #c6f6d5;
  color: #276749;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #48bb78;
}

.format-info {
  background: #f7fafc;
  padding: 20px;
  border-radius: 8px;
  margin-top: 30px;
}

.format-info h3 {
  color: #2d3748;
  margin-bottom: 15px;
}

.format-info ul {
  list-style: none;
  padding-left: 0;
}

.format-info li {
  padding: 8px 0;
  color: #4a5568;
  position: relative;
  padding-left: 25px;
}

.format-info li:before {
  content: "✓";
  position: absolute;
  left: 0;
  color: #48bb78;
  font-weight: bold;
}
</style>