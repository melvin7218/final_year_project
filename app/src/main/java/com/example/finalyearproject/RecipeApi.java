package com.example.finalyearproject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RecipeApi {
    @GET("retrieve_recipe.php") // Path to your API
    Call<RecipeResponse> getRecipe(@Query("id") int recipeId);
}
