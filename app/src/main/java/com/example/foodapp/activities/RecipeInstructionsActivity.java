package com.example.foodapp.activities;

import com.example.foodapp.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeInstructionsActivity extends AppCompatActivity {

    private ImageView btnBack, btnShare, btnFavorite, ivRecipeImage;
    private TextView tvRecipeName, tvRecipeDescription;
    private TextView tabIngredients, tabInstructions, tabNutrition;
    private boolean isFavorite = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_instructions);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnFavorite = findViewById(R.id.btnFavorite);
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvRecipeDescription = findViewById(R.id.tvRecipeDescription);
        tabIngredients = findViewById(R.id.tabIngredients);
        tabInstructions = findViewById(R.id.tabInstructions);
        tabNutrition = findViewById(R.id.tabNutrition);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this recipe: " + tvRecipeName.getText());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        btnFavorite.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
        });

        tabIngredients.setOnClickListener(v -> {
            finish();
        });

        tabNutrition.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeInstructionsActivity.this, NutritionActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Set placeholder image
        ivRecipeImage.setImageResource(R.drawable.img_pho);
    }
}

