package com.jpsoftware.farmapp.shared.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ErrorMessageTranslatorTest {

    @Test
    void shouldTranslatePositiveFieldValidationMessages() {
        assertEquals(
                "A quantidade deve ser maior que zero.",
                ErrorMessageTranslator.translate("quantity must be greater than zero"));
    }

    @Test
    void shouldTranslateScaleFieldValidationMessages() {
        assertEquals(
                "A quantidade deve ter no máximo 2 casas decimais.",
                ErrorMessageTranslator.translate("quantity must have at most 2 decimal places"));
    }

    @Test
    void shouldReturnValidationFallbackForBlankMessages() {
        assertEquals("Falha de validação.", ErrorMessageTranslator.translate("  "));
    }
}
