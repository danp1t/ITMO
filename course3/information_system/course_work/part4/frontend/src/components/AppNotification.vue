<template>
  <transition name="slide-fade">
    <div
      v-if="notification.visible"
      class="notification"
      :class="notification.type"
      @click="hideNotification"
    >
      <div class="notification-content">
        <i class="icon" :class="getIconClass"></i>
        <span>{{ notification.message }}</span>
      </div>
      <button class="notification-close" @click="hideNotification">
        <i class="fas fa-times"></i>
      </button>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Notification {
  visible: boolean
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
}

const props = defineProps<{
  notification: Notification
}>()

const emit = defineEmits<{
  hide: []
}>()

const getIconClass = computed(() => {
  const icons = {
    info: 'fas fa-info-circle',
    success: 'fas fa-check-circle',
    warning: 'fas fa-exclamation-triangle',
    error: 'fas fa-exclamation-circle'
  }
  return icons[props.notification.type]
})

const hideNotification = () => {
  emit('hide')
}
</script>

<style scoped>
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  min-width: 300px;
  max-width: 400px;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  animation: slideIn 0.3s ease;
}

.notification.info {
  background: linear-gradient(135deg, #2d3748 0%, #4a5568 100%);
  border-left: 4px solid #4299e1;
  color: #e2e8f0;
}

.notification.success {
  background: linear-gradient(135deg, #22543d 0%, #38a169 100%);
  border-left: 4px solid #68d391;
  color: #f0fff4;
}

.notification.warning {
  background: linear-gradient(135deg, #744210 0%, #d69e2e 100%);
  border-left: 4px solid #f6e05e;
  color: #fffaf0;
}

.notification.error {
  background: linear-gradient(135deg, #742a2a 0%, #c53030 100%);
  border-left: 4px solid #fc8181;
  color: #fff5f5;
}

.notification-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.notification-content .icon {
  font-size: 1.2rem;
}

.notification-close {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  padding: 4px;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.notification-close:hover {
  opacity: 1;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
