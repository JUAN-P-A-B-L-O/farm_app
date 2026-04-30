import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { CheckboxFilterField, DateFilterField, MultiSelectFilterField, SearchFilterField, SelectFilterField, } from './FilterFields';
function ListingFiltersBar({ search, onApply, onClear, applyLabel, clearLabel, filters = [], }) {
    return (_jsxs("div", { className: "listing-filters", children: [(search || filters.length > 0) && (_jsxs("div", { className: "listing-filters__controls", children: [search ? (_jsx(SearchFilterField, { id: search.id, label: search.label, value: search.value, placeholder: search.placeholder, disabled: search.disabled, helpText: search.helpText, onChange: search.onChange })) : null, filters.map((filter) => {
                        if (filter.type === 'select') {
                            return (_jsx(SelectFilterField, { id: filter.id, label: filter.label, value: filter.value, options: filter.options, disabled: filter.disabled, helpText: filter.helpText, onChange: filter.onChange }, filter.id));
                        }
                        if (filter.type === 'date') {
                            return (_jsx(DateFilterField, { id: filter.id, label: filter.label, value: filter.value, min: filter.min, max: filter.max, disabled: filter.disabled, helpText: filter.helpText, onChange: filter.onChange }, filter.id));
                        }
                        if (filter.type === 'multiselect') {
                            return (_jsx(MultiSelectFilterField, { id: filter.id, label: filter.label, value: filter.value, options: filter.options, size: filter.size, disabled: filter.disabled, helpText: filter.helpText, onChange: filter.onChange }, filter.id));
                        }
                        return (_jsx(CheckboxFilterField, { id: filter.id, label: filter.label, checked: filter.checked, disabled: filter.disabled, helpText: filter.helpText, onChange: filter.onChange }, filter.id));
                    })] })), _jsxs("div", { className: "listing-filters__actions", children: [_jsx("button", { type: "button", className: "animals-table__action-button", onClick: onApply, children: applyLabel }), _jsx("button", { type: "button", className: "animals-table__action-button animals-table__action-button--secondary", onClick: onClear, children: clearLabel })] })] }));
}
export default ListingFiltersBar;
