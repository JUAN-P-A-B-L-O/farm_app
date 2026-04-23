package com.jpsoftware.farmapp.shared.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CsvExportUtilsTest {

    @Test
    void shouldEscapeQuotedCommaAndMultilineValues() {
        List<TestRow> rows = List.of(
                new TestRow("Cow, 1", "He said \"moo\""),
                new TestRow("Cow 2", "Line 1\nLine 2"),
                new TestRow(null, null));

        String csv = CsvExportUtils.write(rows, List.of(
                new CsvColumn<>("name", TestRow::name),
                new CsvColumn<>("notes", TestRow::notes)));

        assertEquals(
                "name,notes\n"
                        + "\"Cow, 1\",\"He said \"\"moo\"\"\"\n"
                        + "Cow 2,\"Line 1\n"
                        + "Line 2\"\n"
                        + ",\n",
                csv);
    }

    private record TestRow(String name, String notes) {
    }
}
