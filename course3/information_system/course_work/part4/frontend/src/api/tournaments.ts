import apiClient from '.'
import type { Tournament, Rang } from '../types/tournaments'

export const tournamentsAPI = {
  getTournaments(params?: { sortBy?: string; sortDirection?: string }) {
    return apiClient.get<Tournament[]>('/api/tournaments', { params })
  },

  createTournament(data: Omit<Tournament, 'id'>) {
    return apiClient.post<Tournament>('/api/tournaments', data)
  },

  updateTournament(id: number, data: Partial<Tournament>) {
    return apiClient.put<Tournament>(`/api/tournaments/${id}`, data)
  },

  deleteTournament(id: number) {
    return apiClient.delete(`/api/tournaments/${id}`)
  },

  getRangs() {
    return apiClient.get<Rang[]>('/api/rangs')
  },
}
