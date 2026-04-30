import type { ReactNode } from 'react'

export interface FilterOption {
  value: string
  label: string
}

interface BaseFilterFieldProps {
  id: string
  label: string
  disabled?: boolean
  helpText?: string
}

interface FilterFieldLabelProps extends BaseFilterFieldProps {
  className?: string
  children: ReactNode
}

interface SearchFilterFieldProps extends BaseFilterFieldProps {
  value: string
  placeholder: string
  onChange: (value: string) => void
}

interface SelectFilterFieldProps extends BaseFilterFieldProps {
  value: string
  options: FilterOption[]
  onChange: (value: string) => void
}

interface DateFilterFieldProps extends BaseFilterFieldProps {
  value: string
  min?: string
  max?: string
  onChange: (value: string) => void
}

interface MultiSelectFilterFieldProps extends BaseFilterFieldProps {
  value: string[]
  options: FilterOption[]
  size?: number
  onChange: (value: string[]) => void
}

interface CheckboxFilterFieldProps extends BaseFilterFieldProps {
  checked: boolean
  onChange: (checked: boolean) => void
}

function FilterFieldLabel({
  id,
  label,
  helpText,
  className,
  children,
}: FilterFieldLabelProps) {
  return (
    <label htmlFor={id} className={className}>
      <span>{label}</span>
      {children}
      {helpText ? <small className="listing-filters__help">{helpText}</small> : null}
    </label>
  )
}

export function SearchFilterField({
  id,
  label,
  value,
  placeholder,
  disabled,
  helpText,
  onChange,
}: SearchFilterFieldProps) {
  return (
    <FilterFieldLabel id={id} label={label} disabled={disabled} helpText={helpText}>
      <input
        id={id}
        type="search"
        value={value}
        disabled={disabled}
        placeholder={placeholder}
        onChange={(event) => onChange(event.target.value)}
      />
    </FilterFieldLabel>
  )
}

export function SelectFilterField({
  id,
  label,
  value,
  options,
  disabled,
  helpText,
  onChange,
}: SelectFilterFieldProps) {
  return (
    <FilterFieldLabel id={id} label={label} disabled={disabled} helpText={helpText}>
      <select
        id={id}
        value={value}
        disabled={disabled}
        onChange={(event) => onChange(event.target.value)}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </FilterFieldLabel>
  )
}

export function DateFilterField({
  id,
  label,
  value,
  min,
  max,
  disabled,
  helpText,
  onChange,
}: DateFilterFieldProps) {
  return (
    <FilterFieldLabel id={id} label={label} disabled={disabled} helpText={helpText}>
      <input
        id={id}
        type="date"
        value={value}
        min={min}
        max={max}
        disabled={disabled}
        onChange={(event) => onChange(event.target.value)}
      />
    </FilterFieldLabel>
  )
}

export function MultiSelectFilterField({
  id,
  label,
  value,
  options,
  size = 4,
  disabled,
  helpText,
  onChange,
}: MultiSelectFilterFieldProps) {
  return (
    <FilterFieldLabel id={id} label={label} disabled={disabled} helpText={helpText}>
      <select
        id={id}
        multiple
        size={size}
        value={value}
        disabled={disabled}
        onChange={(event) => onChange(Array.from(event.target.selectedOptions, (option) => option.value))}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </FilterFieldLabel>
  )
}

export function CheckboxFilterField({
  id,
  label,
  checked,
  disabled,
  helpText,
  onChange,
}: CheckboxFilterFieldProps) {
  return (
    <label htmlFor={id} className="listing-filters__checkbox">
      <span className="listing-filters__checkbox-control">
        <input
          id={id}
          type="checkbox"
          checked={checked}
          disabled={disabled}
          onChange={(event) => onChange(event.target.checked)}
        />
        <span>{label}</span>
      </span>
      {helpText ? <small className="listing-filters__help">{helpText}</small> : null}
    </label>
  )
}
