import apiClient from '.'
import type { Post, Comment, Tag } from '../types/posts'

export const postsAPI = {
  // Получить все посты
  getPosts(params?: { tagId?: number; sortBy?: string; sortDirection?: string }) {
    return apiClient.get<Post[]>('/api/posts', { params })
  },

  // Получить пост по ID
  getPostById(id: number) {
    return apiClient.get<Post>(`/api/posts/${id}`)
  },

  // Создать пост
  createPost(data: { title: string; text: string; ownerId: number }) {
    return apiClient.post<Post>('/api/posts', data)
  },

  // Обновить пост
  updatePost(id: number, data: { title: string; text: string; ownerId: number }) {
    return apiClient.put<Post>(`/api/posts/${id}`, data)
  },

  // Удалить пост
  deletePost(id: number) {
    return apiClient.delete(`/api/posts/${id}`)
  },

  // Поставить лайк
  likePost(postId: number) {
    return apiClient.post(`/api/posts/${postId}/like`)
  },

  // Получить комментарии поста
  getComments(postId: number) {
    return apiClient.get<Comment[]>(`/api/comments?postId=${postId}`)
  },

  // Создать комментарий
  createComment(data: { userComment: string; postId: number; accountId: number }) {
    return apiClient.post<Comment>('/api/comments', data)
  },

  // Удалить комментарий
  deleteComment(id: number) {
    return apiClient.delete(`/api/comments/${id}`)
  },

  // Получить теги
  getTags() {
    return apiClient.get<Tag[]>('/api/tags')
  },
}
