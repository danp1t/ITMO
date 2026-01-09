<template>
  <div class="home dark-theme">
    <section class="hero-section">
      <div class="hero-background">
        <div class="hero-overlay"></div>
        <div class="hero-pattern"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">
          Добро пожаловать в <span class="brand-text">Artistic Rhythmic</span>
        </h1>
        <p class="hero-subtitle">
          Профессиональное сообщество художественной гимнастики
        </p>
        <div class="hero-actions">
          <router-link to="/tournaments" class="button sport-button">
            <span class="icon">
              <i class="fas fa-trophy"></i>
            </span>
            <span>Соревнования</span>
          </router-link>
          <router-link v-if="!authStore.isAuthenticated" to="/register" class="button secondary-button">
            <span class="icon">
              <i class="fas fa-user-plus"></i>
            </span>
            <span>Присоединиться</span>
          </router-link>
        </div>
      </div>
    </section>

    <div class="container">
      <section class="section quick-links">
        <h2 class="title is-3 has-text-centered section-title">Быстрый доступ</h2>
        <div class="columns is-centered">
          <div class="column is-3">
            <router-link to="/posts" class="quick-link-card">
              <div class="quick-link-icon">
                <i class="fas fa-newspaper"></i>
              </div>
              <h3>Лента постов</h3>
              <p>Новости и обсуждения сообщества</p>
            </router-link>
          </div>
          <div class="column is-3">
            <router-link to="/tournaments" class="quick-link-card">
              <div class="quick-link-icon">
                <i class="fas fa-calendar-alt"></i>
              </div>
              <h3>Соревнования</h3>
              <p>Турниры и календарь событий</p>
            </router-link>
          </div>
          <div class="column is-3">
            <router-link v-if="authStore.isAuthenticated" to="/shop" class="quick-link-card">
              <div class="quick-link-icon">
                <i class="fas fa-shopping-bag"></i>
              </div>
              <h3>Магазин</h3>
              <p>Снаряжение и экипировка</p>
            </router-link>
            <div v-else class="quick-link-card disabled">
              <div class="quick-link-icon">
                <i class="fas fa-shopping-bag"></i>
              </div>
              <h3>Магазин</h3>
              <p>Требуется авторизация</p>
            </div>
          </div>
        </div>
      </section>

      <section v-if="stats" class="section stats-section">
        <h2 class="title is-3 has-text-centered section-title">Статистика сообщества</h2>
        <div class="columns is-centered">
          <div class="column is-3">
            <div class="stat-card">
              <div class="stat-number">{{ stats.tournaments || 0 }}</div>
              <div class="stat-label">Соревнований</div>
              <div class="stat-icon">
                <i class="fas fa-trophy"></i>
              </div>
            </div>
          </div>
          <div class="column is-3">
            <div class="stat-card">
              <div class="stat-number">{{ stats.posts || 0 }}</div>
              <div class="stat-label">Постов</div>
              <div class="stat-icon">
                <i class="fas fa-newspaper"></i>
              </div>
            </div>
          </div>
          <div class="column is-3">
            <div class="stat-card">
              <div class="stat-number">{{ stats.products || 0 }}</div>
              <div class="stat-label">Товаров</div>
              <div class="stat-icon">
                <i class="fas fa-shopping-bag"></i>
              </div>
            </div>
          </div>
          <div class="column is-3">
            <div class="stat-card">
              <div class="stat-number">{{ stats.users || 0 }}</div>
              <div class="stat-label">Пользователей</div>
              <div class="stat-icon">
                <i class="fas fa-users"></i>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section v-if="upcomingTournaments.length > 0" class="section events-section">
        <div class="section-header">
          <div class="header-container">
            <div class="header-content">
              <h2 class="title is-3 section-title">Ближайшие события</h2>
              <p class="subtitle is-6 has-text-grey-light">Актуальные турниры и соревнования</p>
            </div>
            <div class="header-action">
              <router-link to="/tournaments" class="link-button">
                Все события
                <span class="icon">
            <i class="fas fa-arrow-right"></i>
          </span>
              </router-link>
            </div>
          </div>
        </div>

        <div class="columns is-multiline is-centered">
          <div
            v-for="tournament in upcomingTournaments.slice(0, 3)"
            :key="tournament.id"
            class="column is-4"
          >
            <div class="event-card">
              <div class="event-date">
                <div class="event-day">{{ formatDay(tournament.startDate) }}</div>
                <div class="event-month">{{ formatMonth(tournament.startDate) }}</div>
                <div class="event-year">{{ formatYear(tournament.startDate) }}</div>
              </div>
              <div class="event-content">
                <div class="event-badge" :class="getRangClass(tournament.rangId)">
                  {{ getRangName(tournament.rangId) }}
                </div>
                <h3 class="event-title">{{ tournament.name }}</h3>
                <p class="event-location">
                  <i class="fas fa-map-marker-alt"></i>
                  {{ tournament.address || 'Место уточняется' }}
                </p>
                <div class="event-details">
                  <div class="event-detail">
                    <i class="fas fa-calendar"></i>
                    <span>{{ formatDateRange(tournament.startDate, tournament.finishDate) }}</span>
                  </div>
                  <div class="event-detail">
                    <i class="fas fa-user"></i>
                    <span>от {{ tournament.minimalAge }} лет</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section v-if="!authStore.isAuthenticated" class="section cta-section">
        <div class="cta-card">
          <div class="cta-icon">
            <i class="fas fa-star"></i>
          </div>
          <div class="cta-content">
            <h2 class="title is-3">Стань частью сообщества</h2>
            <p class="subtitle is-6 has-text-grey-light">
              Присоединяйтесь к крупнейшему сообществу художественной гимнастики в России
            </p>
            <div class="cta-actions">
              <router-link to="/register" class="button sport-button">
                <span class="icon">
                  <i class="fas fa-user-plus"></i>
                </span>
                <span>Зарегистрироваться</span>
              </router-link>
              <router-link to="/login" class="button secondary-button">
                <span class="icon">
                  <i class="fas fa-sign-in-alt"></i>
                </span>
                <span>Войти в аккаунт</span>
              </router-link>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {useAuthStore} from '../stores/auth'
import {tournamentsAPI} from '../api/tournaments'
import type {Rang, Tournament} from '../types/tournaments'

const authStore = useAuthStore()
const upcomingTournaments = ref<Tournament[]>([])
const rangs = ref<Rang[]>([])
const stats = ref({
  tournaments: 0,
  posts: 0,
  products: 0,
  users: 0
})

const loadUpcomingTournaments = async () => {
  try {
    const result = await tournamentsAPI.getTournaments()
    const now = new Date()
    upcomingTournaments.value = result.data.filter((t: Tournament) => {
      const startDate = new Date(t.startDate)
      return startDate > now && !t.archived
    }).slice(0, 3)
    stats.value.tournaments = result.data.length
  } catch (error) {
    console.error('Ошибка при загрузке турниров:', error)
  }
}

const loadRangs = async () => {
  try {
    const response = await tournamentsAPI.getRangs()
    rangs.value = response.data
  } catch (error) {
    console.error('Ошибка при загрузке рангов:', error)
  }
}

const formatDay = (dateString: string) => {
  const date = new Date(dateString)
  return date.getDate().toString().padStart(2, '0')
}

const formatMonth = (dateString: string) => {
  const date = new Date(dateString)
  const months = [
    'янв', 'фев', 'мар', 'апр', 'мая', 'июн',
    'июл', 'авг', 'сен', 'окт', 'ноя', 'дек'
  ]
  return months[date.getMonth()]
}

const formatYear = (dateString: string) => {
  const date = new Date(dateString)
  return date.getFullYear()
}

const formatDateRange = (startDate: string, finishDate: string) => {
  const start = new Date(startDate)
  const finish = new Date(finishDate)

  if (start.getMonth() === finish.getMonth() && start.getFullYear() === finish.getFullYear()) {
    return `${start.getDate()}-${finish.getDate()} ${formatMonth(startDate)}`
  }

  return `${formatDay(startDate)} ${formatMonth(startDate)} - ${formatDay(finishDate)} ${formatMonth(finishDate)}`
}

const getRangName = (rangId: number) => {
  const rang = rangs.value.find(r => r.id === rangId)
  return rang ? rang.name : 'Не указано'
}

const getRangClass = (rangId: number) => {
  const rang = rangs.value.find(r => r.id === rangId)
  if (!rang) return 'rang-default'

  switch (rang.name.toLowerCase()) {
    case 'международный':
      return 'rang-international'
    case 'всероссийский':
      return 'rang-national'
    case 'региональный':
      return 'rang-regional'
    case 'городской':
      return 'rang-city'
    default:
      return 'rang-default'
  }
}

const loadStats = async () => {
  stats.value = {
    tournaments: 24,
    posts: 156,
    products: 89,
    users: 342
  }
}

onMounted(() => {
  loadUpcomingTournaments()
  loadRangs()
  loadStats()
})
</script>

<style scoped>
.dark-theme {
  background: #0f172a;
  color: #e2e8f0;
}

.home {
  min-height: 100vh;
}

.hero-section {
  position: relative;
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 6rem 1rem;
  overflow: hidden;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 80%, rgba(56, 189, 248, 0.1) 0%, transparent 50%),
  radial-gradient(circle at 80% 20%, rgba(74, 222, 128, 0.1) 0%, transparent 50%);
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(15, 23, 42, 0.8), rgba(15, 23, 42, 0.95));
}

.hero-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 1px 1px, rgba(255, 255, 255, 0.05) 1px, transparent 0);
  background-size: 40px 40px;
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 900px;
  margin: 0 auto;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 800;
  margin-bottom: 1.5rem;
  line-height: 1.1;
  color: #f8fafc;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.brand-text {
  background: linear-gradient(135deg, #38bdf8 0%, #4ade80 50%, #fbbf24 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  display: inline-block;
}

.brand-text::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(135deg, #38bdf8 0%, #4ade80 50%, #fbbf24 100%);
  border-radius: 1px;
}

.hero-subtitle {
  font-size: 1.5rem;
  margin-bottom: 3rem;
  color: #cbd5e1;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 1.5rem;
  justify-content: center;
  flex-wrap: wrap;
}

.sport-button {
  background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%);
  border: none;
  color: white;
  font-weight: 600;
  padding: 1rem 2rem;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(14, 165, 233, 0.2);
}

.sport-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 25px rgba(14, 165, 233, 0.3);
  background: linear-gradient(135deg, #0284c7 0%, #2563eb 100%);
}

.secondary-button {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(71, 85, 105, 0.5);
  color: #cbd5e1;
  font-weight: 500;
  padding: 1rem 2rem;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.secondary-button:hover {
  background: rgba(51, 65, 85, 0.8);
  border-color: rgba(100, 116, 139, 0.5);
  color: #f1f5f9;
  transform: translateY(-3px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.section {
  padding: 4rem 0;
}

.section-title {
  color: #f8fafc;
  margin-bottom: 3rem;
  position: relative;
  padding-bottom: 1rem;
}

.header-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 3rem;
  position: relative;
}

.header-content {
  max-width: 600px;
  margin: 0 auto 1.5rem;
}

.header-action {
  margin-top: 1rem;
}

.section-title {
  color: #f8fafc;
  margin-bottom: 0.5rem;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%);
  border-radius: 2px;
}

.columns.is-centered {
  justify-content: center;
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 1rem;
  }

  .header-content {
    width: 100%;
  }

  .column.is-4 {
    display: flex;
    justify-content: center;
  }

  .event-card {
    max-width: 100%;
    width: 100%;
  }
}

@media (max-width: 1024px) {
  .column.is-4 {
    width: 50%;
  }
}

@media (max-width: 768px) {
  .column.is-4 {
    width: 100%;
  }
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%);
  border-radius: 2px;
}

.quick-links {
  margin-top: -4rem;
  position: relative;
  z-index: 10;
}

.quick-link-card {
  display: block;
  background: rgba(30, 41, 59, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(71, 85, 105, 0.3);
  border-radius: 16px;
  padding: 2.5rem 2rem;
  text-align: center;
  color: inherit;
  text-decoration: none;
  height: 100%;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.quick-link-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%);
  transform: translateY(-4px);
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.quick-link-card:hover {
  transform: translateY(-10px);
  border-color: rgba(14, 165, 233, 0.5);
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(14, 165, 233, 0.1);
}

.quick-link-card:hover::before {
  transform: translateY(0);
}

.quick-link-card.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  filter: grayscale(0.5);
}

.quick-link-card.disabled:hover {
  transform: none;
  border-color: rgba(71, 85, 105, 0.3);
  box-shadow: none;
}

.quick-link-card.disabled:hover::before {
  transform: translateY(-4px);
}

.quick-link-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 2rem;
  background: rgba(14, 165, 233, 0.1);
  border: 1px solid rgba(14, 165, 233, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #38bdf8;
  font-size: 2rem;
  transition: all 0.3s;
}

.quick-link-card:hover .quick-link-icon {
  background: rgba(14, 165, 233, 0.2);
  transform: scale(1.1);
  color: #0ea5e9;
}

.quick-link-card h3 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #f8fafc;
}

.quick-link-card p {
  color: #94a3b8;
  font-size: 0.95rem;
  line-height: 1.6;
}

.stats-section {
  background: rgba(15, 23, 42, 0.5);
  border-radius: 24px;
  margin: 3rem 0;
  position: relative;
  overflow: hidden;
}

.stats-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.3), transparent);
}

.stat-card {
  text-align: center;
  padding: 2.5rem 1.5rem;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(71, 85, 105, 0.2);
  border-radius: 16px;
  position: relative;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  border-color: rgba(14, 165, 233, 0.3);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.stat-number {
  font-size: 3.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #38bdf8 0%, #4ade80 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 1rem;
  position: relative;
  display: inline-block;
}

.stat-number::after {
  content: '+';
  position: absolute;
  top: 0;
  right: -1rem;
  font-size: 2rem;
  color: #4ade80;
  opacity: 0.7;
}

.stat-label {
  font-size: 0.9rem;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 1.5rem;
}

.stat-icon {
  width: 50px;
  height: 50px;
  margin: 0 auto;
  background: rgba(14, 165, 233, 0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #38bdf8;
  font-size: 1.5rem;
}

.events-section {
  margin: 4rem 0;
}

.section-header {
  margin-bottom: 3rem;
}

.link-button {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: #38bdf8;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  background: rgba(14, 165, 233, 0.1);
}

.link-button:hover {
  background: rgba(14, 165, 233, 0.2);
  color: #0ea5e9;
  transform: translateX(5px);
}

.event-card {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(71, 85, 105, 0.3);
  border-radius: 16px;
  overflow: hidden;
  height: 100%;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.event-card:hover {
  transform: translateY(-8px);
  border-color: rgba(14, 165, 233, 0.5);
  box-shadow:
    0 20px 40px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(14, 165, 233, 0.1);
}

.event-date {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  padding: 1.5rem;
  text-align: center;
  border-bottom: 1px solid rgba(71, 85, 105, 0.3);
}

.event-day {
  font-size: 2.5rem;
  font-weight: 800;
  color: #38bdf8;
  line-height: 1;
  margin-bottom: 0.25rem;
}

.event-month {
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #94a3b8;
  margin-bottom: 0.25rem;
}

.event-year {
  font-size: 0.875rem;
  color: #64748b;
}

.event-content {
  padding: 1.5rem;
}

.event-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 1rem;
}


.event-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #f8fafc;
  line-height: 1.4;
}

.event-location {
  color: #94a3b8;
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.event-location i {
  color: #38bdf8;
}

.event-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.event-detail {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #94a3b8;
  font-size: 0.9rem;
}

.event-detail i {
  color: #4ade80;
  width: 16px;
}

.cta-section {
  margin: 4rem 0;
}

.cta-card {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.9) 0%, rgba(15, 23, 42, 0.9) 100%);
  border: 1px solid rgba(71, 85, 105, 0.3);
  border-radius: 20px;
  padding: 4rem;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 3rem;
}

.cta-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(135deg, #38bdf8 0%, #4ade80 50%, #fbbf24 100%);
}

.cta-icon {
  width: 100px;
  height: 100px;
  background: rgba(14, 165, 233, 0.1);
  border: 1px solid rgba(14, 165, 233, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #38bdf8;
  font-size: 3rem;
  flex-shrink: 0;
}

.cta-content {
  flex: 1;
}

.cta-content .title {
  color: #f8fafc;
  margin-bottom: 1rem;
}

.cta-content .subtitle {
  color: #94a3b8;
  margin-bottom: 2rem;
  max-width: 500px;
}

.cta-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

@media (max-width: 1024px) {
  .hero-title {
    font-size: 2.8rem;
  }

  .column.is-3 {
    width: 50%;
    margin-bottom: 2rem;
  }

  .stat-card {
    padding: 2rem 1rem;
  }

  .stat-number {
    font-size: 3rem;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.2rem;
  }

  .hero-subtitle {
    font-size: 1.25rem;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
    width: 100%;
  }

  .hero-actions .button {
    width: 100%;
    max-width: 300px;
    margin-bottom: 1rem;
  }

  .columns.is-centered {
    flex-direction: column;
  }

  .column.is-3,
  .column.is-4 {
    width: 100%;
    margin-bottom: 2rem;
  }

  .cta-card {
    flex-direction: column;
    text-align: center;
    padding: 3rem 2rem;
  }

  .cta-actions {
    justify-content: center;
  }

  .cta-actions .button {
    width: 100%;
    max-width: 250px;
  }

  .event-card {
    max-width: 400px;
    margin: 0 auto;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 1.8rem;
  }

  .hero-subtitle {
    font-size: 1.1rem;
  }

  .section {
    padding: 3rem 0;
  }

  .section-title {
    font-size: 1.5rem;
  }

  .quick-link-card,
  .stat-card,
  .event-card {
    padding: 1.5rem;
  }
}
</style>
