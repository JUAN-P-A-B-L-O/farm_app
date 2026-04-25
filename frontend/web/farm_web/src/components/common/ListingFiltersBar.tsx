interface FilterOption {
  value: string
  label: string
}

interface BaseFilterConfig {
  id: string
  label: string
  value: string
  onChange: (value: string) => void
}

interface SelectFilterConfig extends BaseFilterConfig {
  options: FilterOption[]
}

interface DateFilterConfig extends BaseFilterConfig {
  max?: string
}

interface ListingFiltersBarProps {
  searchId: string
  searchLabel: string
  searchPlaceholder: string
  searchValue: string
  onSearchChange: (value: string) => void
  onApply: () => void
  onClear: () => void
  applyLabel: string
  clearLabel: string
  filters?: Array<SelectFilterConfig | DateFilterConfig>
}

function ListingFiltersBar({
  searchId,
  searchLabel,
  searchPlaceholder,
  searchValue,
  onSearchChange,
  onApply,
  onClear,
  applyLabel,
  clearLabel,
  filters = [],
}: ListingFiltersBarProps) {
  return (
    <div className="listing-filters">
      <div className="listing-filters__controls">
        <label htmlFor={searchId}>
          {searchLabel}
          <input
            id={searchId}
            type="search"
            value={searchValue}
            placeholder={searchPlaceholder}
            onChange={(event) => onSearchChange(event.target.value)}
          />
        </label>

        {filters.map((filter) => (
          <label key={filter.id} htmlFor={filter.id}>
            {filter.label}
            {'options' in filter ? (
              <select
                id={filter.id}
                value={filter.value}
                onChange={(event) => filter.onChange(event.target.value)}
              >
                {filter.options.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            ) : (
              <input
                id={filter.id}
                type="date"
                value={filter.value}
                max={filter.max}
                onChange={(event) => filter.onChange(event.target.value)}
              />
            )}
          </label>
        ))}
      </div>

      <div className="listing-filters__actions">
        <button type="button" className="animals-table__action-button" onClick={onApply}>
          {applyLabel}
        </button>
        <button
          type="button"
          className="animals-table__action-button animals-table__action-button--secondary"
          onClick={onClear}
        >
          {clearLabel}
        </button>
      </div>
    </div>
  )
}

export default ListingFiltersBar
