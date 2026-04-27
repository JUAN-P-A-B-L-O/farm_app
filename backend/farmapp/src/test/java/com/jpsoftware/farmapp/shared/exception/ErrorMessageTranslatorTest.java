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
    void shouldTranslateEmptyFieldValidationMessages() {
        assertEquals("O nome é obrigatório.", ErrorMessageTranslator.translate("name must not be empty"));
    }

    @Test
    void shouldTranslateSecurityMessages() {
        assertEquals("Não autorizado.", ErrorMessageTranslator.translate("Unauthorized"));
        assertEquals("Acesso negado.", ErrorMessageTranslator.translate("Access is denied"));
        assertEquals("Acesso negado.", ErrorMessageTranslator.translate("Access Denied"));
        assertEquals("Acesso negado.", ErrorMessageTranslator.translate("Forbidden"));
        assertEquals(
                "É necessário autenticação para acessar este recurso.",
                ErrorMessageTranslator.translate("Full authentication is required to access this resource"));
    }

    @Test
    void shouldTranslateMapperMessages() {
        assertEquals("Animal obrigatório.", ErrorMessageTranslator.translate("Animal entity must not be null"));
    }

    @Test
    void shouldReturnValidationFallbackForBlankMessages() {
        assertEquals("Falha de validação.", ErrorMessageTranslator.translate("  "));
    }
}
