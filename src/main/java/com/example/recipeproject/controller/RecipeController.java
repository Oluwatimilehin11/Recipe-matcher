package com.example.recipeproject.controller;

import com.example.recipeproject.dto.CreateRecipeRequest;
import com.example.recipeproject.dto.RecipeMatchResponse;
import com.example.recipeproject.dto.RecipeResponse;
import com.example.recipeproject.dto.SearchRequest;
import com.example.recipeproject.model.Recipe;
import com.example.recipeproject.model.RecipeIngredient;
import com.example.recipeproject.repository.RecipeRepository;
import com.example.recipeproject.service.RecipeMatcherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeMatcherService recipeMatcherService;
    private final RecipeRepository recipeRepository;

    @PostMapping("/match")
    public List<RecipeMatchResponse> matchRecipes(@Valid @RequestBody SearchRequest request) {
        return recipeMatcherService.findMatchingRecipes(request);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<RecipeResponse> getAllRecipes() {
        return recipeRepository.findAllWithIngredients().stream()
                .map(RecipeResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public RecipeResponse getRecipeById(@PathVariable Long id) {
        return recipeRepository.findById(id)
                .map(RecipeResponse::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found with id: " + id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody CreateRecipeRequest request) {
        Recipe recipe = new Recipe();
        recipe.setTitle(request.title().trim());
        recipe.setBaseServings(request.baseServings());
        recipe.setSourceUrl(request.sourceUrl());

        List<RecipeIngredient> ingredients = new ArrayList<>();
        for (CreateRecipeRequest.IngredientInput input : request.ingredients()) {
            RecipeIngredient ri = new RecipeIngredient();
            ri.setIngredientName(input.name().trim());
            ri.setBaseAmount(input.baseAmount());
            ri.setUnit(input.unit());
            ri.setRecipe(recipe);
            ingredients.add(ri);
        }
        recipe.setIngredients(ingredients);

        Recipe saved = recipeRepository.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).body(RecipeResponse.fromEntity(saved));
    }
}