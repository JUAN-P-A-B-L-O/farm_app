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
                "É necessário estar autenticado para acessar este recurso.",
                ErrorMessageTranslator.translate("Full authentication is required to access this resource"));
        assertEquals(
                "É necessário estar autenticado como gerente.",
                ErrorMessageTranslator.translate("Authenticated manager is required"));
        assertEquals(
                "É necessário estar autenticado.",
                ErrorMessageTranslator.translate("Authenticated user is required"));
    }

    @Test
    void shouldTranslateMapperMessages() {
        assertEquals("Animal obrigatório.", ErrorMessageTranslator.translate("Animal entity must not be null"));
    }

    @Test
    void shouldTranslateAnimalFieldValidationMessagesWithDomainPrefixes() {
        assertEquals(
                "A data de nascimento do animal é obrigatória.",
                ErrorMessageTranslator.translate("Animal birthDate must not be null"));
        assertEquals(
                "O status do animal é obrigatório.",
                ErrorMessageTranslator.translate("Animal status must not be blank"));
        assertEquals(
                "O valor da venda deve ser maior que zero.",
                ErrorMessageTranslator.translate("Animal salePrice must be greater than zero"));
    }

    @Test
    void shouldTranslateMeasurementUnitValidationMessages() {
        assertEquals(
                "A unidade deve ser LITER ou MILLILITER.",
                ErrorMessageTranslator.translate("measurementUnit must be LITER or MILLILITER"));
        assertEquals(
                "A unidade deve ser KILOGRAM ou GRAM.",
                ErrorMessageTranslator.translate("measurementUnit must be KILOGRAM or GRAM"));
        assertEquals(
                "A unidade de produção deve ser LITER ou MILLILITER.",
                ErrorMessageTranslator.translate("productionUnit must be LITER or MILLILITER"));
    }

    @Test
    void shouldReturnValidationFallbackForBlankMessages() {
        assertEquals("Falha de validação.", ErrorMessageTranslator.translate("  "));
    }
}
