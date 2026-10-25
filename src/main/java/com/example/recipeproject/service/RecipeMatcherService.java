package com.example.recipeproject.service;

import com.example.recipeproject.dto.RecipeMatchResponse;
import com.example.recipeproject.dto.SearchRequest;
import com.example.recipeproject.model.Recipe;
import com.example.recipeproject.model.RecipeIngredient;
import com.example.recipeproject.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeMatcherService {

    private final RecipeRepository recipeRepository;
    private final WebRecipeSearchService webRecipeSearchService;

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
                // 1. Try finding local database alternatives for this serving size
                List<RecipeMatchResponse.AlternativeRecipeLinkDto> alternativeLinks =
                        findAlternativeLinks(recipe.getId(), request.targetServings(), userInventory, allRecipes);

                // 2. If no matching single-serve alternatives exist locally, search the web live
                if (alternativeLinks.isEmpty()) {
                    List<String> ingredientNames = request.availableIngredients().stream()
                            .map(SearchRequest.UserIngredientDto::name)
                            .toList();

                    alternativeLinks = webRecipeSearchService.searchExternalRecipes(
                            recipe.getTitle(),
                            request.targetServings(),
                            ingredientNames
                    );
                }

                matches.add(new RecipeMatchResponse(
                        recipe.getId(),
                        recipe.getTitle(),
                        recipe.getSourceUrl(),
                        extractDomain(recipe.getSourceUrl()),
                        Math.round(matchPercentage * 10.0) / 10.0,
                        request.targetServings(),
                        scaledList,
                        alternativeLinks
                ));
            }
        }

        matches.sort((a, b) -> Double.compare(b.matchPercentage(), a.matchPercentage()));
        return matches;
    }

    private List<RecipeMatchResponse.AlternativeRecipeLinkDto> findAlternativeLinks(
            Long currentRecipeId,
            int targetServings,
            Map<String, Double> userInventory,
            List<Recipe> allRecipes
    ) {
        return allRecipes.stream()
                .filter(r -> !Objects.equals(r.getId(), currentRecipeId))
                .filter(r -> Math.abs(r.getBaseServings() - targetServings) <= 1)
                .map(r -> {
                    List<String> missing = new ArrayList<>();
                    double coverageTotal = 0.0;

                    for (RecipeIngredient ri : r.getIngredients()) {
                        String name = ri.getIngredientName().trim().toLowerCase();
                        double userHas = userInventory.getOrDefault(name, 0.0);

                        if (userHas < ri.getBaseAmount()) {
                            missing.add(ri.getIngredientName());
                        }

                        double cov = (ri.getBaseAmount() == 0) ? 1.0 : Math.min(1.0, userHas / ri.getBaseAmount());
                        coverageTotal += cov;
                    }

                    double altScore = r.getIngredients().isEmpty() ? 0.0 :
                            (coverageTotal / r.getIngredients().size()) * 100.0;

                    return new RecipeMatchResponse.AlternativeRecipeLinkDto(
                            r.getTitle(),
                            r.getSourceUrl(),
                            extractDomain(r.getSourceUrl()),
                            r.getBaseServings(),
                            Math.round(altScore * 10.0) / 10.0,
                            missing
                    );
                })
                .sorted((a, b) -> Double.compare(b.matchPercentage(), a.matchPercentage()))
                .limit(4)
                .collect(Collectors.toList());
    }

    private String extractDomain(String url) {
        if (url == null || url.isBlank()) {
            return "Internal Recipe";
        }
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            if (domain == null) return "Recipe Source";
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (Exception e) {
            return "Recipe Source";
        }
    }
}