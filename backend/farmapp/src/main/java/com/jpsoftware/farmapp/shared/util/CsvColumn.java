package com.jpsoftware.farmapp.shared.util;

import java.util.function.Function;

public record CsvColumn<T>(String header, Function<T, ?> valueExtractor) {
}
