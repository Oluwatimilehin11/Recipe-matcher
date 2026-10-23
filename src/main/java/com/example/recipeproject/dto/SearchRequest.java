package com.example.recipeproject.dto;

import com.example.recipeproject.model.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record SearchRequest(
        @NotNull(message = "Target servings is required")
        @Positive(message = "Target servings must be greater than 0")
        Integer targetServings,

        @NotEmpty(message = "Available ingredients list cannot be empty")
        @Valid
        List<UserIngredientDto> availableIngredients
) {
    public record UserIngredientDto(
            @NotBlank(message = "Ingredient name cannot be blank")
            String name,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be greater than 0")
            Double amount,

            @NotNull(message = "Unit is required")
            Unit unit
    ) {}
}