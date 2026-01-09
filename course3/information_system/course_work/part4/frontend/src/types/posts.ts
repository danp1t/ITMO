export interface Post {
  id: number
  title: string
  text: string
  ownerId: number
  ownerName: string
  createdAt: string
  updatedAt: string
  countLike: number
  commentsCount?: number
  tags?: Tag[]
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

export interface UpdatePostRequest {
  title: string
  text: string
  ownerId: number
  tags?: Tag[];
}
