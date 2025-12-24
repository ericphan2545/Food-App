package com.example.foodapp.services;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

import com.example.foodapp.models.Recipe;

public interface RecipeApiService {
    // Base URL: https://692d9d81e5f67cd80a4c3fa9.mockapi.io/foodib/api/v1/
    // Endpoint: food
    @GET("food")
    Call<List<Recipe>> getRecipes();
}
