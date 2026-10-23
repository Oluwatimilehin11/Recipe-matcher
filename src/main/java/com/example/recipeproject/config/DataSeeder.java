package com.example.recipeproject.config;

import com.example.recipeproject.model.Recipe;
import com.example.recipeproject.model.RecipeIngredient;
import com.example.recipeproject.model.Unit;
import com.example.recipeproject.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RecipeRepository recipeRepository;

    @Override
    public void run(String... args) {
        if (recipeRepository.count() > 0) {
            recipeRepository.deleteAll(); // refresh with the new bakery categories
        }

        List<Recipe> recipes = new ArrayList<>();

        // 1. Classic Banana Bread
        Recipe bananaBread = new Recipe();
        bananaBread.setTitle("Classic Banana Bread");
        bananaBread.setBaseServings(8);
        bananaBread.setSourceUrl("https://example.com/banana-bread");
        List<RecipeIngredient> bbIng = List.of(
                createIng(bananaBread, "banana", 3.0, Unit.PIECE),
                createIng(bananaBread, "flour", 250.0, Unit.GRAM),
                createIng(bananaBread, "butter", 115.0, Unit.GRAM),
                createIng(bananaBread, "sugar", 150.0, Unit.GRAM),
                createIng(bananaBread, "egg", 2.0, Unit.PIECE)
        );
        bananaBread.setIngredients(bbIng);
        recipes.add(bananaBread);

        // 2. Moist Chocolate Cake
        Recipe chocCake = new Recipe();
        chocCake.setTitle("Moist Chocolate Cake");
        chocCake.setBaseServings(8);
        chocCake.setSourceUrl("https://example.com/chocolate-cake");
        List<RecipeIngredient> cakeIng = List.of(
                createIng(chocCake, "flour", 200.0, Unit.GRAM),
                createIng(chocCake, "sugar", 200.0, Unit.GRAM),
                createIng(chocCake, "cocoa powder", 50.0, Unit.GRAM),
                createIng(chocCake, "egg", 2.0, Unit.PIECE),
                createIng(chocCake, "milk", 240.0, Unit.MILLILITER),
                createIng(chocCake, "butter", 100.0, Unit.GRAM)
        );
        chocCake.setIngredients(cakeIng);
        recipes.add(chocCake);

        // 3. Crusty Artisan Bread
        Recipe bread = new Recipe();
        bread.setTitle("Crusty Artisan Bread");
        bread.setBaseServings(6);
        bread.setSourceUrl("https://example.com/artisan-bread");
        List<RecipeIngredient> breadIng = List.of(
                createIng(bread, "flour", 450.0, Unit.GRAM),
                createIng(bread, "water", 350.0, Unit.MILLILITER),
                createIng(bread, "yeast", 7.0, Unit.GRAM),
                createIng(bread, "salt", 8.0, Unit.GRAM)
        );
        bread.setIngredients(breadIng);
        recipes.add(bread);

        // 4. Butter Shortbread Cookies
        Recipe cookies = new Recipe();
        cookies.setTitle("Butter Shortbread Cookies");
        cookies.setBaseServings(4);
        cookies.setSourceUrl("https://example.com/shortbread");
        List<RecipeIngredient> cookieIng = List.of(
                createIng(cookies, "flour", 250.0, Unit.GRAM),
                createIng(cookies, "sugar", 75.0, Unit.GRAM),
                createIng(cookies, "butter", 175.0, Unit.GRAM)
        );
        cookies.setIngredients(cookieIng);
        recipes.add(cookies);

        recipeRepository.saveAll(recipes);
    }

    private RecipeIngredient createIng(Recipe r, String name, Double amount, Unit unit) {
        RecipeIngredient ri = new RecipeIngredient();
        ri.setRecipe(r);
        ri.setIngredientName(name);
        ri.setBaseAmount(amount);
        ri.setUnit(unit);
        return ri;
    }
}