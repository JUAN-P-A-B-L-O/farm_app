import {
  CheckboxFilterField,
  DateFilterField,
  MultiSelectFilterField,
  SearchFilterField,
  SelectFilterField,
  type FilterOption,
} from './FilterFields'

interface SearchFilterConfig {
  id: string
  label: string
  value: string
  placeholder: string
  disabled?: boolean
  helpText?: string
  onChange: (value: string) => void
}

interface BaseFilterConfig {
  id: string
  label: string
  disabled?: boolean
  helpText?: string
}

interface SelectFilterConfig extends BaseFilterConfig {
  type: 'select'
  value: string
  options: FilterOption[]
  onChange: (value: string) => void
}

interface DateFilterConfig extends BaseFilterConfig {
  type: 'date'
  value: string
  min?: string
  max?: string
  onChange: (value: string) => void
}

interface MultiSelectFilterConfig extends BaseFilterConfig {
  type: 'multiselect'
  value: string[]
  options: FilterOption[]
  size?: number
  onChange: (value: string[]) => void
}

interface CheckboxFilterConfig extends BaseFilterConfig {
  type: 'checkbox'
  checked: boolean
  onChange: (checked: boolean) => void
}

type ListingFilterConfig =
  | SelectFilterConfig
  | DateFilterConfig
  | MultiSelectFilterConfig
  | CheckboxFilterConfig

interface ListingFiltersBarProps {
  search?: SearchFilterConfig
  onClear: () => void
  clearLabel: string
  filters?: ListingFilterConfig[]
}

function ListingFiltersBar({
  search,
  onClear,
  clearLabel,
  filters = [],
}: ListingFiltersBarProps) {
  return (
    <div className="listing-filters">
      {(search || filters.length > 0) && (
        <div className="listing-filters__controls">
          {search ? (
            <SearchFilterField
              id={search.id}
              label={search.label}
              value={search.value}
              placeholder={search.placeholder}
              disabled={search.disabled}
              helpText={search.helpText}
              onChange={search.onChange}
            />
          ) : null}

          {filters.map((filter) => {
            if (filter.type === 'select') {
              return (
                <SelectFilterField
                  key={filter.id}
                  id={filter.id}
                  label={filter.label}
                  value={filter.value}
                  options={filter.options}
                  disabled={filter.disabled}
                  helpText={filter.helpText}
                  onChange={filter.onChange}
                />
              )
            }

            if (filter.type === 'date') {
              return (
                <DateFilterField
                  key={filter.id}
                  id={filter.id}
                  label={filter.label}
                  value={filter.value}
                  min={filter.min}
                  max={filter.max}
                  disabled={filter.disabled}
                  helpText={filter.helpText}
                  onChange={filter.onChange}
                />
              )
            }

            if (filter.type === 'multiselect') {
              return (
                <MultiSelectFilterField
                  key={filter.id}
                  id={filter.id}
                  label={filter.label}
                  value={filter.value}
                  options={filter.options}
                  size={filter.size}
                  disabled={filter.disabled}
                  helpText={filter.helpText}
                  onChange={filter.onChange}
                />
              )
            }

            return (
              <CheckboxFilterField
                key={filter.id}
                id={filter.id}
                label={filter.label}
                checked={filter.checked}
                disabled={filter.disabled}
                helpText={filter.helpText}
                onChange={filter.onChange}
              />
            )
          })}
        </div>
      )}

      <div className="listing-filters__actions">
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
