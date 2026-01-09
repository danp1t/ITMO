import apiClient from '.'
import type {Tag} from '../types/posts'

export const tagsAPI = {
  async getAllTags() {
    return await apiClient.get<Tag[]>('/api/tags')
  },

  async getPostTags(postId: number) {
    return await apiClient.get<Tag[]>(`/api/tags/post/${postId}`)
  },
}
