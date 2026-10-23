package com.example.recipeproject.service;

import com.example.recipeproject.dto.RecipeMatchResponse;
import com.example.recipeproject.dto.SearchRequest;
import com.example.recipeproject.model.Recipe;
import com.example.recipeproject.model.RecipeIngredient;
import com.example.recipeproject.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeMatcherService {

    private final RecipeRepository recipeRepository;

    @Transactional(readOnly = true)
    public List<RecipeMatchResponse> findMatchingRecipes(SearchRequest request) {
        List<Recipe> allRecipes = recipeRepository.findAllWithIngredients();

        Map<String, Double> userInventory = request.availableIngredients().stream()
                .collect(Collectors.toMap(
                        i -> i.name().trim().toLowerCase(),
                        SearchRequest.UserIngredientDto::amount,
                        Double::sum
                ));

        List<RecipeMatchResponse> matches = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            double scaleFactor = (double) request.targetServings() / recipe.getBaseServings();
            double totalCoverage = 0.0;
            List<RecipeMatchResponse.ScaledIngredientDto> scaledList = new ArrayList<>();

            for (RecipeIngredient ri : recipe.getIngredients()) {
                String name = ri.getIngredientName().trim().toLowerCase();
                double scaledRequired = ri.getBaseAmount() * scaleFactor;
                double userHas = userInventory.getOrDefault(name, 0.0);

                boolean sufficient = userHas >= scaledRequired;
                double coverage = (scaledRequired == 0) ? 1.0 : Math.min(1.0, userHas / scaledRequired);
                totalCoverage += coverage;

                scaledList.add(new RecipeMatchResponse.ScaledIngredientDto(
                        ri.getIngredientName(),
                        Math.round(scaledRequired * 100.0) / 100.0,
                        ri.getUnit().name(),
                        userHas,
                        sufficient
                ));
            }

            double matchPercentage = recipe.getIngredients().isEmpty() ? 0.0 :
                    (totalCoverage / recipe.getIngredients().size()) * 100.0;

            if (matchPercentage > 0) {
                matches.add(new RecipeMatchResponse(
                        recipe.getId(),
                        recipe.getTitle(),
                        recipe.getSourceUrl(),
                        Math.round(matchPercentage * 10.0) / 10.0,
                        request.targetServings(),
                        scaledList
                ));
            }
        }

        matches.sort((a, b) -> Double.compare(b.matchPercentage(), a.matchPercentage()));
        return matches;
    }
}