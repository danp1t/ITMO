import apiClient from '.'
import type { Tag } from '../types/posts'

export const tagsAPI = {
  // Получить все теги
  async getAllTags() {
    const response = await apiClient.get<Tag[]>('/api/tags')
    return response
  },

  // Получить теги поста
  async getPostTags(postId: number) {
    const response = await apiClient.get<Tag[]>(`/api/tags/post/${postId}`)
    return response
  },

  // Создать тег
  createTag(data: { name: string; description?: string }) {
    return apiClient.post<Tag>('/api/tags', data)
  },

  // Обновить тег
  updateTag(id: number, data: { name: string; description?: string }) {
    return apiClient.put<Tag>(`/api/tags/${id}`, data)
  },

  // Удалить тег
  deleteTag(id: number) {
    return apiClient.delete(`/api/tags/${id}`)
  },

  // Добавить тег к посту
  addTagToPost(postId: number, tagId: number) {
    return apiClient.post(`/api/posts/${postId}/tags/${tagId}`)
  },

  // Удалить тег у поста
  removeTagFromPost(postId: number, tagId: number) {
    return apiClient.delete(`/api/posts/${postId}/tags/${tagId}`)
  },

  // Получить популярные теги
  async getPopularTags(limit: number = 10) {
    const response = await apiClient.get<Tag[]>('/api/tags/popular', {
      params: { limit }
    })
    return response
  }
}
