import apiClient from '.'
import type {Comment, Post} from '../types/posts'

export const postsAPI = {
  async getPosts(params?: { tagId?: number; sortBy?: string; sortDirection?: string }) {
    const response = await apiClient.get<any[]>('/api/posts', { params })

    const posts: Post[] = response.data.map(post => ({
      ...post,
      tags: post.tags || [],
      commentsCount: post.commentsCount || 0
    }))

    return { ...response, data: posts }
  },

  async getPostById(id: number) {
    const response = await apiClient.get<any>(`/api/posts/${id}`)
    const post: Post = {
      ...response.data,
      tags: response.data.tags || [],
      commentsCount: response.data.commentsCount || 0
    }

    return { ...response, data: post }
  },

  createPost(data: { title: string; text: string; ownerId: number }) {
    return apiClient.post<Post>('/api/posts', data)
  },

  updatePost(id: number, data: { title: string; text: string; ownerId: number }) {
    return apiClient.put<Post>(`/api/posts/${id}`, data)
  },

  deletePost(id: number) {
    return apiClient.delete(`/api/posts/${id}`)
  },

  likePost(postId: number) {
    return apiClient.post(`/api/posts/${postId}/like`)
  },

  async getComments(postId: number) {
    return await apiClient.get<Comment[]>(`/api/comments/post/${postId}`)
  },

  async createComment(data: { userComment: string; postId: number; accountId: number }) {
    try {
      return await apiClient.post<Comment>('/api/comments', data)
    } catch (error) {
      console.error('Error creating comment:', error)
      throw error
    }
  },

  async updateComment(id: number, comment: Comment) {
    return await apiClient.put<Comment>(`/api/comments/${id}`, {
      id: comment.id,
      createdAt: comment.createdAt,
      userComment: comment.userComment,
      postId: comment.postId,
      accountId: comment.accountId,
      accountName: comment.accountName
    })
  },

  deleteComment(id: number) {
    return apiClient.delete(`/api/comments/${id}`)
  },

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
