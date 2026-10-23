package com.example.recipeproject.dto;

import java.util.List;

public record RecipeMatchResponse(
        Long recipeId,
        String title,
        String sourceUrl,
        Double matchPercentage,
        Integer targetServings,
        List<ScaledIngredientDto> scaledIngredients
) {
    public record ScaledIngredientDto(
            String name,
            Double requiredAmount,
            String unit,
            Double availableAmount,
    boolean isSufficient
    ) {}
}