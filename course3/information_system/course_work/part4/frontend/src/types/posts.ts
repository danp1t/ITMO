export interface Post {
  id: number
  title: string
  text: string
  ownerId: number
  ownerName: string
  createdAt: string
  updatedAt: string
  countLike: number  // Изменено с likesCount на countLike
  commentsCount?: number  // Оставляем опциональным, так как в ответе его нет
  tags?: Tag[]     // Оставляем опциональным
}

export interface Tag {
  id: number
  name: string
  description: string
  color?: string
}

export interface Comment {
  id: number
  userComment: string
  postId: number
  accountId: number
  accountName: string
  createdAt: string
  updatedAt?: string
  canEdit?: boolean
  canDelete?: boolean
}

export interface CreatePostData {
  title: string
  text: string
  ownerId: number
  tagIds?: number[]
}

export interface CreateCommentData {
  userComment: string
  postId: number
  accountId: number
}

export interface UpdatePostRequest {
  title: string
  text: string
  ownerId: number
  tagIds?: number[]
}
