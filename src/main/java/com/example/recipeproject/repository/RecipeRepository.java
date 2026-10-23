package com.example.recipeproject.repository;

import com.example.recipeproject.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.ingredients")
    List<Recipe> findAllWithIngredients();
}