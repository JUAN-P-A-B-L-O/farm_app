import { useTranslation } from '../../hooks/useTranslation'
import type { PaginatedResponse } from '../../types/pagination'

interface PaginationControlsProps {
  pagination: PaginatedResponse<unknown>
  isLoading?: boolean
  onPageChange: (page: number) => void
  onPageSizeChange: (size: number) => void
}

const PAGE_SIZE_OPTIONS = [10, 25, 50]

function PaginationControls({
  pagination,
  isLoading = false,
  onPageChange,
  onPageSizeChange,
}: PaginationControlsProps) {
  const { t } = useTranslation()

  if (pagination.totalElements === 0) {
    return null
  }

  const currentPage = pagination.page
  const totalPages = Math.max(pagination.totalPages, 1)
  const from = currentPage * pagination.size + 1
  const to = Math.min((currentPage + 1) * pagination.size, pagination.totalElements)
  const canGoPrevious = currentPage > 0
  const canGoNext = currentPage + 1 < totalPages

  return (
    <div className="pagination-controls">
      <p className="pagination-controls__summary">
        {t('common.pagination.summary', {
          from,
          to,
          total: pagination.totalElements,
        })}
      </p>

      <div className="pagination-controls__actions">
        <label className="pagination-controls__size" htmlFor="pagination-size">
          <span>{t('common.pagination.pageSize')}</span>
          <select
            id="pagination-size"
            value={pagination.size}
            onChange={(event) => onPageSizeChange(Number(event.target.value))}
            disabled={isLoading}
          >
            {PAGE_SIZE_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>

        <span className="pagination-controls__page">
          {t('common.pagination.page', {
            page: currentPage + 1,
            totalPages,
          })}
        </span>

        <div className="pagination-controls__buttons">
          <button
            type="button"
            className="animals-table__action-button animals-table__action-button--secondary"
            onClick={() => onPageChange(currentPage - 1)}
            disabled={isLoading || !canGoPrevious}
          >
            {t('common.pagination.previous')}
          </button>

          <button
            type="button"
            className="animals-table__action-button"
            onClick={() => onPageChange(currentPage + 1)}
            disabled={isLoading || !canGoNext}
          >
            {t('common.pagination.next')}
          </button>
        </div>
      </div>
    </div>
  )
}

export default PaginationControls
