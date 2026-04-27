package com.jpsoftware.farmapp.shared.exception;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ErrorMessageTranslator {

    private static final String VALIDATION_FALLBACK = "Falha de validação.";
    private static final Pattern REQUIRED_FIELD_PATTERN =
            Pattern.compile("^(?:Animal )?([A-Za-z]+) must not be (?:blank|null)$");
    private static final Pattern EMPTY_FIELD_PATTERN =
            Pattern.compile("^([A-Za-z]+) must not be empty$");
    private static final Pattern POSITIVE_FIELD_PATTERN =
            Pattern.compile("^(?:Animal )?([A-Za-z]+) must be greater than zero$");
    private static final Pattern SCALE_FIELD_PATTERN =
            Pattern.compile("^([A-Za-z]+) must have at most 2 decimal places$");

    private static final Map<String, String> EXACT_TRANSLATIONS = Map.ofEntries(
            Map.entry("Validation failed", VALIDATION_FALLBACK),
            Map.entry("Malformed request body", "Corpo da requisição inválido."),
            Map.entry("Internal server error", "Erro interno do servidor."),
            Map.entry("Resource not found", "Recurso não encontrado."),
            Map.entry("Unauthorized", "Não autorizado."),
            Map.entry("Access is denied", "Acesso negado."),
            Map.entry("Full authentication is required to access this resource", "É necessário autenticação para acessar este recurso."),
            Map.entry("Invalid or expired token", "Token inválido ou expirado."),
            Map.entry("request must not be null", "Requisição obrigatória."),
            Map.entry("Create animal request must not be null", "Requisição obrigatória."),
            Map.entry("Update animal request must not be null", "Requisição obrigatória."),
            Map.entry("Sell animal request must not be null", "Requisição obrigatória."),
            Map.entry("Authenticated manager is required", "Gerente autenticado obrigatório."),
            Map.entry("Authenticated user is required", "Usuário autenticado obrigatório."),
            Map.entry("Authenticated user is required to register milk price", "É necessário estar autenticado para registrar o preço do leite."),
            Map.entry("currentPassword is incorrect", "A senha atual está incorreta."),
            Map.entry("newPassword must be different from currentPassword", "A nova senha deve ser diferente da senha atual."),
            Map.entry("farmIds must reference farms owned by the authenticated manager", "Selecione apenas fazendas pertencentes ao gerente autenticado."),
            Map.entry("User with this email already exists", "Já existe um usuário com este e-mail."),
            Map.entry("password must not be blank when active is true", "A senha é obrigatória para usuários ativos."),
            Map.entry("User who owns farms must remain a manager", "Usuários proprietários de fazendas devem permanecer com a função de gerente."),
            Map.entry("Inactive user who owns farms cannot be updated", "Não é possível atualizar um usuário inativo que possui fazendas."),
            Map.entry("id must be a valid UUID", "Informe um identificador válido."),
            Map.entry("userId must be a valid UUID", "Selecione um usuário válido."),
            Map.entry("Farm not found", "Fazenda não encontrada."),
            Map.entry("User not found", "Usuário não encontrado."),
            Map.entry("Animal not found", "Animal não encontrado."),
            Map.entry("Feed type not found", "Tipo de ração não encontrado."),
            Map.entry("Production not found", "Produção não encontrada."),
            Map.entry("Feeding not found", "Alimentação não encontrada."),
            Map.entry("Date cannot be in the future", "A data não pode estar no futuro."),
            Map.entry("Animal must be ACTIVE for production operations", "O animal deve estar ativo para registrar produção."),
            Map.entry("Animal must be ACTIVE for feeding operations", "O animal deve estar ativo para registrar alimentação."),
            Map.entry("Animal with this tag already exists", "Já existe um animal com esta tag."),
            Map.entry("Animal status must be ACTIVE, SOLD, DEAD, or INACTIVE", "O status do animal deve ser Ativo, Vendido, Morto ou Inativo."),
            Map.entry("Use the sell action to mark an animal as SOLD", "Use a ação de venda para marcar o animal como vendido."),
            Map.entry("Sold animals cannot transition to another status", "Animais vendidos não podem mudar para outro status."),
            Map.entry("Animal origin must be PURCHASED or BORN", "A origem do animal deve ser Comprado ou Nascido."),
            Map.entry("Animal acquisitionCost must be greater than zero for purchased animals", "O custo de aquisição deve ser maior que zero para animais comprados."),
            Map.entry("Animal is already sold", "O animal já foi vendido."),
            Map.entry("Only active animals can be sold", "Apenas animais ativos podem ser vendidos."),
            Map.entry("startDate must be before or equal to endDate", "A data inicial deve ser anterior ou igual à data final."),
            Map.entry("groupBy must be day or month", "O agrupamento deve ser por dia ou mês."),
            Map.entry("status must be ACTIVE, INACTIVE, SOLD, or DEAD", "O status deve ser Ativo, Inativo, Vendido ou Morto."),
            Map.entry("currency must be BRL or USD", "A moeda deve ser BRL ou USD."),
            Map.entry("Invalid email or password", "E-mail ou senha inválidos."),
            Map.entry("Inactive production cannot be updated", "Não é possível atualizar uma produção inativa."),
            Map.entry("Inactive feeding cannot be updated", "Não é possível atualizar uma alimentação inativa."),
            Map.entry("Cannot inactivate user who owns farms", "Não é possível inativar um usuário que possui fazendas."),
            Map.entry("Cannot delete user who owns farms", "Não é possível excluir um usuário que possui fazendas."),
            Map.entry("Data integrity violation", "Violação de integridade de dados."),
            Map.entry("must be greater than 0", "O valor deve ser maior que zero."));

    private ErrorMessageTranslator() {
    }

    public static String translate(String message) {
        if (message == null || message.isBlank()) {
            return VALIDATION_FALLBACK;
        }

        String trimmedMessage = message.trim();
        String exactTranslation = EXACT_TRANSLATIONS.get(trimmedMessage);
        if (exactTranslation != null) {
            return exactTranslation;
        }

        String requiredFieldTranslation = translateFieldMessage(trimmedMessage, REQUIRED_FIELD_PATTERN, TranslationKind.REQUIRED);
        if (requiredFieldTranslation != null) {
            return requiredFieldTranslation;
        }

        String emptyFieldTranslation = translateFieldMessage(trimmedMessage, EMPTY_FIELD_PATTERN, TranslationKind.REQUIRED);
        if (emptyFieldTranslation != null) {
            return emptyFieldTranslation;
        }

        String positiveFieldTranslation = translateFieldMessage(trimmedMessage, POSITIVE_FIELD_PATTERN, TranslationKind.POSITIVE);
        if (positiveFieldTranslation != null) {
            return positiveFieldTranslation;
        }

        String scaleFieldTranslation = translateFieldMessage(trimmedMessage, SCALE_FIELD_PATTERN, TranslationKind.SCALE);
        if (scaleFieldTranslation != null) {
            return scaleFieldTranslation;
        }

        return trimmedMessage;
    }

    private static String translateFieldMessage(String message, Pattern pattern, TranslationKind translationKind) {
        Matcher matcher = pattern.matcher(message);
        if (!matcher.matches()) {
            return null;
        }

        String fieldName = matcher.group(1);
        return switch (translationKind) {
            case REQUIRED -> translateRequiredField(fieldName);
            case POSITIVE -> translatePositiveField(fieldName);
            case SCALE -> translateScaleField(fieldName);
        };
    }

    private static String translateRequiredField(String fieldName) {
        return switch (fieldName) {
            case "name" -> "O nome é obrigatório.";
            case "email" -> "O e-mail é obrigatório.";
            case "password" -> "A senha é obrigatória.";
            case "currentPassword" -> "A senha atual é obrigatória.";
            case "newPassword" -> "A nova senha é obrigatória.";
            case "role" -> "A função é obrigatória.";
            case "farmIds" -> "Selecione ao menos uma fazenda.";
            case "active" -> "Informe se o usuário está ativo.";
            case "farmId" -> "Selecione uma fazenda.";
            case "animalId" -> "Selecione um animal.";
            case "feedTypeId" -> "Selecione um tipo de ração.";
            case "userId" -> "Selecione um usuário.";
            case "quantity" -> "A quantidade é obrigatória.";
            case "price" -> "O preço é obrigatório.";
            case "effectiveDate" -> "A data de vigência é obrigatória.";
            case "date" -> "A data é obrigatória.";
            case "costPerKg" -> "O custo por kg é obrigatório.";
            case "acquisitionCost" -> "O custo de aquisição é obrigatório.";
            case "salePrice" -> "O valor da venda é obrigatório.";
            case "tag" -> "A tag do animal é obrigatória.";
            case "breed" -> "A raça do animal é obrigatória.";
            case "birthDate" -> "A data de nascimento do animal é obrigatória.";
            case "origin" -> "A origem do animal é obrigatória.";
            case "id" -> "O identificador é obrigatório.";
            case "request" -> "Requisição obrigatória.";
            default -> null;
        };
    }

    private static String translatePositiveField(String fieldName) {
        return switch (fieldName) {
            case "quantity" -> "A quantidade deve ser maior que zero.";
            case "price" -> "O preço deve ser maior que zero.";
            case "costPerKg" -> "O custo por kg deve ser maior que zero.";
            case "acquisitionCost" -> "O custo de aquisição deve ser maior que zero.";
            case "salePrice" -> "O valor da venda deve ser maior que zero.";
            default -> null;
        };
    }

    private static String translateScaleField(String fieldName) {
        return switch (fieldName) {
            case "quantity" -> "A quantidade deve ter no máximo 2 casas decimais.";
            case "price" -> "O preço deve ter no máximo 2 casas decimais.";
            case "costPerKg" -> "O custo por kg deve ter no máximo 2 casas decimais.";
            case "acquisitionCost" -> "O custo de aquisição deve ter no máximo 2 casas decimais.";
            case "salePrice" -> "O valor da venda deve ter no máximo 2 casas decimais.";
            default -> null;
        };
    }

    private enum TranslationKind {
        REQUIRED,
        POSITIVE,
        SCALE
    }
}
