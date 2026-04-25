import type { PaginatedResponse } from '../types/pagination'

export const DEFAULT_PAGE_SIZE = 10

export function createEmptyPaginatedResponse<T>(size = DEFAULT_PAGE_SIZE): PaginatedResponse<T> {
  return {
    content: [],
    page: 0,
    size,
    totalElements: 0,
    totalPages: 0,
  }
}
