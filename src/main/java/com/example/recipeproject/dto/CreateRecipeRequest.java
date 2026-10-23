package com.example.recipeproject.dto;

import com.example.recipeproject.model.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateRecipeRequest(
        @NotBlank(message = "Title cannot be blank")
        String title,

        @NotNull(message = "Base servings is required")
        @Positive(message = "Base servings must be greater than 0")
        Integer baseServings,

        String sourceUrl,

        @NotEmpty(message = "Ingredients list cannot be empty")
        @Valid
        List<IngredientInput> ingredients
) {
    public record IngredientInput(
            @NotBlank(message = "Ingredient name cannot be blank")
            String name,

            @NotNull(message = "Base amount is required")
            @Positive(message = "Base amount must be greater than 0")
            Double baseAmount,

            @NotNull(message = "Unit is required")
            Unit unit
    ) {}
}