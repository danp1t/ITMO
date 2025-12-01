import apiClient from '.'
import type {Comment, Post} from '../types/posts'

export const postsAPI = {
  // Получить все посты
  async getPosts(params?: { tagId?: number; sortBy?: string; sortDirection?: string }) {
    const response = await apiClient.get<any[]>('/api/posts', { params })

    // Преобразуем данные из сервера в наш формат
    const posts: Post[] = response.data.map(post => ({
      ...post,
      // Убедимся, что у нас есть все необходимые поля
      tags: post.tags || [],
      commentsCount: post.commentsCount || 0
    }))

    return { ...response, data: posts }
  },

  // Получить пост по ID
  async getPostById(id: number) {
    const response = await apiClient.get<any>(`/api/posts/${id}`)

    // Преобразуем данные
    const post: Post = {
      ...response.data,
      tags: response.data.tags || [],
      commentsCount: response.data.commentsCount || 0
    }

    return { ...response, data: post }
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
  async getComments(postId: number) {
    const response = await apiClient.get<Comment[]>(`/api/comments?postId=${postId}`)
    return response
  },

  // Создать комментарий
  async createComment(data: { userComment: string; postId: number; accountId: number }) {
    try {
      return await apiClient.post<Comment>('/api/comments', data)
    } catch (error) {
      console.error('Error creating comment:', error)
      throw error
    }
  },

  // Удалить комментарий
  deleteComment(id: number) {
    return apiClient.delete(`/api/comments/${id}`)
  },

  // Фильтрация по тегам
  async getPostsByTag(tagId: number) {
    const response = await apiClient.get<any[]>(`/api/posts/tag/${tagId}`)

    const posts: Post[] = response.data.map(post => ({
      ...post,
      tags: post.tags || [],
      commentsCount: post.commentsCount || 0
    }))

    return { ...response, data: posts }
  },
}
