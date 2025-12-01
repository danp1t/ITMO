export interface Tournament {
  id: number
  name: string
  startDate: string
  finishDate: string
  address: string
  link: string
  rangId: number
  rangName: string
  minimalAge: number
  archived: boolean
}

export interface Rang {
  id: number
  name: string
  description: string
}
