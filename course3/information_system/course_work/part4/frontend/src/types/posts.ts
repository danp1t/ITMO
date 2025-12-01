export interface Post {
  id: number
  title: string
  text: string
  ownerId: number
  ownerName: string
  createdAt: string
  updatedAt: string
  likesCount: number
  commentsCount: number
  tags: string[]
  attachments?: Attachment[]
}

export interface Comment {
  id: number
  userComment: string
  postId: number
  accountId: number
  accountName: string
  createdAt: string
}

export interface Attachment {
  id: number
  postId: number
  name: string
  path: string
  typeAttachmentId: number
  url?: string // Для фронтенда - полный URL к файлу
}

export interface Tag {
  id: number
  name: string
  description?: string
  postCount?: number
}

export interface Like {
  id: number
  postId: number
  accountId: number
  createdAt: string
}

export interface CreatePostData {
  title: string
  text: string
  ownerId: number
  tags?: string[]
}

export interface UpdatePostData {
  title?: string
  text?: string
  ownerId?: number
  tags?: string[]
}

export interface CreateCommentData {
  userComment: string
  postId: number
  accountId: number
}

export interface CreateAttachmentData {
  postId: number
  name: string
  path: string
  typeAttachmentId: number
}
