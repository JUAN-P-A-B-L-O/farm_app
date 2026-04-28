import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
function FilterFieldLabel({ id, label, helpText, className, children, }) {
    return (_jsxs("label", { htmlFor: id, className: className, children: [_jsx("span", { children: label }), children, helpText ? _jsx("small", { className: "listing-filters__help", children: helpText }) : null] }));
}
export function SearchFilterField({ id, label, value, placeholder, disabled, helpText, onChange, }) {
    return (_jsx(FilterFieldLabel, { id: id, label: label, disabled: disabled, helpText: helpText, children: _jsx("input", { id: id, type: "search", value: value, disabled: disabled, placeholder: placeholder, onChange: (event) => onChange(event.target.value) }) }));
}
export function SelectFilterField({ id, label, value, options, disabled, helpText, onChange, }) {
    return (_jsx(FilterFieldLabel, { id: id, label: label, disabled: disabled, helpText: helpText, children: _jsx("select", { id: id, value: value, disabled: disabled, onChange: (event) => onChange(event.target.value), children: options.map((option) => (_jsx("option", { value: option.value, children: option.label }, option.value))) }) }));
}
export function DateFilterField({ id, label, value, min, max, disabled, helpText, onChange, }) {
    return (_jsx(FilterFieldLabel, { id: id, label: label, disabled: disabled, helpText: helpText, children: _jsx("input", { id: id, type: "date", value: value, min: min, max: max, disabled: disabled, onChange: (event) => onChange(event.target.value) }) }));
}
export function MultiSelectFilterField({ id, label, value, options, size = 4, disabled, helpText, onChange, }) {
    return (_jsx(FilterFieldLabel, { id: id, label: label, disabled: disabled, helpText: helpText, children: _jsx("select", { id: id, multiple: true, size: size, value: value, disabled: disabled, onChange: (event) => onChange(Array.from(event.target.selectedOptions, (option) => option.value)), children: options.map((option) => (_jsx("option", { value: option.value, children: option.label }, option.value))) }) }));
}
export function CheckboxFilterField({ id, label, checked, disabled, helpText, onChange, }) {
    return (_jsxs("label", { htmlFor: id, className: "listing-filters__checkbox", children: [_jsxs("span", { className: "listing-filters__checkbox-control", children: [_jsx("input", { id: id, type: "checkbox", checked: checked, disabled: disabled, onChange: (event) => onChange(event.target.checked) }), _jsx("span", { children: label })] }), helpText ? _jsx("small", { className: "listing-filters__help", children: helpText }) : null] }));
}
