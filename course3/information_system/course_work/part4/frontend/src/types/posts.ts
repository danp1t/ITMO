export interface Post {
  id: number
  title: string
  text: string
  ownerId: number
  ownerName: string
  createdAt: string
  countLike: number  // Изменено с likesCount на countLike
  commentsCount?: number  // Оставляем опциональным, так как в ответе его нет
  tags?: string[]  // Оставляем опциональным
}

export interface Comment {
  id: number
  userComment: string
  postId: number
  accountId: number
  accountName: string
  createdAt: string
}

export interface CreatePostData {
  title: string
  text: string
  ownerId: number
}

export interface CreateCommentData {
  userComment: string
  postId: number
  accountId: number
}
