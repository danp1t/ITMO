import apiClient from '.'
import type { Tournament, Rang } from '../types/tournaments'

export const tournamentsAPI = {
  // Получить все турниры
  getTournaments(params?: { sortBy?: string; sortDirection?: string }) {
    return apiClient.get<Tournament[]>('/api/tournaments', { params })
  },

  // Поиск турниров по названию
  searchTournaments(name: string, sortBy?: string, sortDirection?: string) {
    return apiClient.get<Tournament[]>('/api/tournaments/search', {
      params: { name, sortBy, sortDirection },
    })
  },

  // Получить турниры по рангу
  getTournamentsByRang(rangId: number) {
    return apiClient.get<Tournament[]>(`/api/tournaments/rang/${rangId}`)
  },

  // Получить турнир по ID
  getTournamentById(id: number) {
    return apiClient.get<Tournament>(`/api/tournaments/${id}`)
  },

  // АСоздать турнир
  createTournament(data: Omit<Tournament, 'id'>) {
    return apiClient.post<Tournament>('/api/tournaments', data)
  },

  // Обновить турнир
  updateTournament(id: number, data: Partial<Tournament>) {
    return apiClient.put<Tournament>(`/api/tournaments/${id}`, data)
  },

  // Удалить турнир
  deleteTournament(id: number) {
    return apiClient.delete(`/api/tournaments/${id}`)
  },

  // Архивация старых турниров
  archiveOldTournaments() {
    return apiClient.post('/api/tournaments/archive-old')
  },

  // Получить все ранги
  getRangs() {
    return apiClient.get<Rang[]>('/api/rangs')
  },

  // Создать ранг
  createRang(data: Omit<Rang, 'id'>) {
    return apiClient.post<Rang>('/api/rangs', data)
  },

  // Обновить ранг
  updateRang(id: number, data: Partial<Rang>) {
    return apiClient.put<Rang>(`/api/rangs/${id}`, data)
  },

  // Удалить ранг
  deleteRang(id: number) {
    return apiClient.delete(`/api/rangs/${id}`)
  },
}
