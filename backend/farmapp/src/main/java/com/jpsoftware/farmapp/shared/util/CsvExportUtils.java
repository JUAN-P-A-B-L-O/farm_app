package com.jpsoftware.farmapp.shared.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public final class CsvExportUtils {

    private CsvExportUtils() {
    }

    public static <T> String write(Collection<T> rows, List<CsvColumn<T>> columns) {
        StringBuilder builder = new StringBuilder();
        appendRow(builder, columns.stream().map(CsvColumn::header).toList());

        for (T row : rows) {
            appendRow(builder, columns.stream()
                    .map(column -> formatValue(column.valueExtractor().apply(row)))
                    .toList());
        }

        return builder.toString();
    }

    private static void appendRow(StringBuilder builder, List<String> values) {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(escape(values.get(index)));
        }
        builder.append('\n');
    }

    private static String formatValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof Double doubleValue) {
            return BigDecimal.valueOf(doubleValue).toPlainString();
        }
        if (value instanceof Float floatValue) {
            return BigDecimal.valueOf(floatValue.doubleValue()).toPlainString();
        }
        return value == null ? "" : value.toString();
    }

    private static String escape(String value) {
        boolean mustQuote = value.contains(",")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r");
        if (!mustQuote) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
