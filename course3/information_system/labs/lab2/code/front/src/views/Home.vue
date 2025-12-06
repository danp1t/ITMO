<script>
import Header from "@/components/Header.vue";
import OrganizationsTable from '@/components/OrganizationTable.vue'
import SpecialOperations from '@/components/SpecialOperations.vue'
import MassImport from '@/components/Import.vue'
import ImportHistory from '@/components/ImportHistory.vue'
import Login from '@/components/LoginForm.vue'
import Register from '@/components/RegisterForm.vue'

export default {
  components: {
    OrganizationsTable,
    SpecialOperations,
    MassImport,
    ImportHistory,
    Login,
    Register,
    Header
  },
  data() {
    return {
      currentView: 'organizations',
      isAuthenticated: false,
      user: null,
      showAuth: false,
      authMode: 'login' // 'login' или 'register'
    }
  },
  methods: {
    handleLoginSuccess(userData) {
      this.isAuthenticated = true
      this.user = userData
      this.showAuth = false
      this.authMode = 'login'
      console.log('Пользователь авторизован:', userData)
    },

    handleLogout() {
      this.isAuthenticated = false
      this.user = null
      console.log('Пользователь вышел')
    },

    switchAuthMode(mode) {
      this.authMode = mode
    },

    showLogin() {
      this.showAuth = true
      this.authMode = 'login'
    },

    showRegister() {
      this.showAuth = true
      this.authMode = 'register'
    }
  }
}
</script>

<template>
  <Header
      :isAuthenticated="isAuthenticated"
      :user="user"
      @login="showLogin"
      @register="showRegister"
      @logout="handleLogout"
  />

  <div v-if="showAuth" class="auth-overlay">
    <div class="auth-modal">
      <Login
          v-if="authMode === 'login'"
          @loginSuccess="handleLoginSuccess"
          @switchToRegister="switchAuthMode('register')"
      />
      <Register
          v-else
          @switchToLogin="switchAuthMode('login')"
      />
      <button @click="showAuth = false" class="close-auth">×</button>
    </div>
  </div>

  <div v-else>
    <nav class="main-nav">
      <button
          @click="currentView = 'organizations'"
          :class="{ active: currentView === 'organizations' }"
      >
        Таблица организаций
      </button>
      <button
          @click="currentView = 'specialOperations'"
          :class="{ active: currentView === 'specialOperations' }"
      >
        Специальные операции
      </button>
      <button
          @click="currentView = 'massImport'"
          :class="{ active: currentView === 'massImport' }"
      >
        Импорт XML
      </button>
      <button
          @click="currentView = 'importHistory'"
          :class="{ active: currentView === 'importHistory' }"
      >
        История импорта
      </button>
    </nav>

    <OrganizationsTable v-if="currentView === 'organizations'" />
    <SpecialOperations v-if="currentView === 'specialOperations'" />
    <MassImport v-if="currentView === 'massImport'" />
    <ImportHistory v-if="currentView === 'importHistory'" />
  </div>
</template>

<style scoped>
.main-nav {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-bottom: 1px solid #e1e5e9;
  flex-wrap: wrap;
  gap: 10px;
}

.main-nav button {
  padding: 12px 24px;
  margin: 0 5px;
  border: 2px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  flex: 1;
  min-width: 160px;
  max-width: 200px;
}

.main-nav button:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.main-nav button.active {
  background: #667eea;
  color: white;
}

.auth-overlay {
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

.auth-modal {
  position: relative;
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

.close-auth {
  position: absolute;
  top: -40px;
  right: 0;
  background: white;
  border: none;
  font-size: 30px;
  color: white;
  cursor: pointer;
  padding: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.close-auth:hover {
  background: #f7fafc;
}

@media (max-width: 768px) {
  .main-nav {
    flex-direction: column;
    align-items: center;
  }

  .main-nav button {
    width: 100%;
    max-width: 300px;
    margin: 5px 0;
  }
}
</style>