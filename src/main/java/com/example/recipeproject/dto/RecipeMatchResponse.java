package com.example.recipeproject.dto;

import java.util.List;

public record RecipeMatchResponse(
        Long recipeId,
        String title,
        String sourceUrl,
        String sourceDomain,               // e.g. "sallysbakingaddiction.com"
        Double matchPercentage,
        Integer targetServings,
        List<ScaledIngredientDto> scaledIngredients,
        List<AlternativeRecipeLinkDto> alternativeLinks // Direct external options matching servings & pantry
) {
    public record ScaledIngredientDto(
            String name,
            Double requiredAmount,
            String unit,
            Double availableAmount,
            boolean isSufficient
    ) {}

    public record AlternativeRecipeLinkDto(
            String title,
            String sourceUrl,
            String sourceDomain,
            Integer servings,
            Double matchPercentage,
            List<String> missingIngredients
    ) {}
}