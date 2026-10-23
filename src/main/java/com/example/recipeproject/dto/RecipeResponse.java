package com.example.recipeproject.dto;

import com.example.recipeproject.model.Recipe;
import com.example.recipeproject.model.Unit;

import java.util.List;

public record RecipeResponse(
        Long id,
        String title,
        int baseServings,
        String sourceUrl,
        List<IngredientDto> ingredients
) {
    public record IngredientDto(
            Long id,
            String name,
            Double baseAmount,
            Unit unit
    ) {}

    public static RecipeResponse fromEntity(Recipe recipe) {
        List<IngredientDto> ingredientDtos = recipe.getIngredients() == null ? List.of() :
                recipe.getIngredients().stream()
                        .map(i -> new IngredientDto(i.getId(), i.getIngredientName(), i.getBaseAmount(), i.getUnit()))
                        .toList();

        return new RecipeResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getBaseServings(),
                recipe.getSourceUrl(),
                ingredientDtos
        );
    }
}