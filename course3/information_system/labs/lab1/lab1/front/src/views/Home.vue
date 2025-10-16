
<script>
import Header from "@/components/Header.vue";
import OrganizationsTable from '@/components/OrganizationTable.vue'
import SpecialOperations from '@/components/SpecialOperations.vue'

export default {
  components: {
    OrganizationsTable,
    SpecialOperations
  },
  data() {
    return {
      currentView: 'organizations'
    }
  }
}
</script>

<template>
  <div>
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
    </nav>

    <OrganizationsTable v-if="currentView === 'organizations'" />
    <SpecialOperations v-if="currentView === 'specialOperations'" />
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
}

.main-nav button {
  padding: 12px 24px;
  margin: 0 10px;
  border: 2px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
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
</style>
