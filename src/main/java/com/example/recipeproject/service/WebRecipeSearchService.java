package com.example.recipeproject.service;

import com.example.recipeproject.dto.RecipeMatchResponse.AlternativeRecipeLinkDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebRecipeSearchService {

    private final RestClient restClient;

    @Value("${search.google.api-key:}")
    private String apiKey;

    @Value("${search.google.cx:}")
    private String searchEngineId;

    public WebRecipeSearchService() {
        this.restClient = RestClient.create();
    }

    public List<AlternativeRecipeLinkDto> searchExternalRecipes(
            String recipeType,
            int servings,
            List<String> ingredients
    ) {
        List<AlternativeRecipeLinkDto> results = new ArrayList<>();

        if (apiKey.isBlank() || searchEngineId.isBlank()) {
            return results;
        }

        String servingConstraint = (servings == 1)
                ? "(\"single serve\" OR \"for one\" OR \"1 serving\")"
                : "(\"" + servings + " servings\")";

        String ingredientList = String.join(" ", ingredients);
        String query = recipeType + " " + servingConstraint + " " + ingredientList + " recipe";

        try {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("www.googleapis.com")
                            .path("/customsearch/v1")
                            .queryParam("key", apiKey)
                            .queryParam("cx", searchEngineId)
                            .queryParam("q", query)
                            .queryParam("num", 4)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("items")) {
                for (JsonNode item : response.get("items")) {
                    String title = item.path("title").asText();
                    String link = item.path("link").asText();
                    String domain = extractDomain(link);

                    results.add(new AlternativeRecipeLinkDto(
                            cleanTitle(title),
                            link,
                            domain,
                            servings,
                            90.0,
                            List.of()
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Web search failed: " + e.getMessage());
        }

        return results;
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return (domain != null && domain.startsWith("www.")) ? domain.substring(4) : domain;
        } catch (Exception e) {
            return "Web Source";
        }
    }

    private String cleanTitle(String raw) {
        return raw.split(" [|\\-–—] ")[0];
    }
}