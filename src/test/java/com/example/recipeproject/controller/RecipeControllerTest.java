package com.example.recipeproject.controller;

import com.example.recipeproject.repository.RecipeRepository;
import com.example.recipeproject.service.RecipeMatcherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeMatcherService recipeMatcherService;

    @MockBean
    private RecipeRepository recipeRepository;

    @Test
    @DisplayName("POST /api/recipes/match with invalid payload returns 400 Bad Request")
    void testInvalidPayloadReturnsBadRequest() throws Exception {
        String invalidJson = """
            {
              "targetServings": 0,
              "availableIngredients": []
            }
            """;

        mockMvc.perform(post("/api/recipes/match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.targetServings").exists())
                .andExpect(jsonPath("$.errors.availableIngredients").exists());
    }
}