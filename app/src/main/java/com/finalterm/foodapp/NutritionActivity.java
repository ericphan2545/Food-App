package com.example.foodapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class NutritionActivity extends AppCompatActivity {

    private ImageView btnBack, ivRecipeImage;
    private TextView tvRecipeName, tvCalorieValue;
    private TextView tvProteinValue, tvProteinPercent;
    private TextView tvCarbsValue, tvCarbsPercent;
    private TextView tvFatValue, tvFatPercent;
    private ProgressBar progressProtein, progressCarbs, progressFat;
    
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        initViews();
        loadRecipeData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvCalorieValue = findViewById(R.id.tvCalorieValue);
        tvProteinValue = findViewById(R.id.tvProteinValue);
        tvProteinPercent = findViewById(R.id.tvProteinPercent);
        tvCarbsValue = findViewById(R.id.tvCarbsValue);
        tvCarbsPercent = findViewById(R.id.tvCarbsPercent);
        tvFatValue = findViewById(R.id.tvFatValue);
        tvFatPercent = findViewById(R.id.tvFatPercent);
        progressProtein = findViewById(R.id.progressProtein);
        progressCarbs = findViewById(R.id.progressCarbs);
        progressFat = findViewById(R.id.progressFat);
    }

    private void loadRecipeData() {
        recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        
        if (recipe != null) {
            tvRecipeName.setText(recipe.getName());
            
            // Load image - priority: Base64 > URL
            if (recipe.getImageBase64() != null && !recipe.getImageBase64().isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(recipe.getImageBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        ivRecipeImage.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    ivRecipeImage.setImageResource(R.drawable.img_placeholder);
                }
            } else if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(recipe.getImageUrl())
                        .centerCrop()
                        .placeholder(R.drawable.img_placeholder)
                        .into(ivRecipeImage);
            }
            
            // Set calories
            int calories = recipe.getCalories();
            tvCalorieValue.setText(String.valueOf(calories));
            
            // Calculate nutrition based on calories (estimated values)
            // Typical distribution: 20% protein, 50% carbs, 30% fat
            int protein = recipe.getProtein() > 0 ? recipe.getProtein() : (int) (calories * 0.2 / 4); // 4 cal per gram
            int carbs = recipe.getCarbs() > 0 ? recipe.getCarbs() : (int) (calories * 0.5 / 4); // 4 cal per gram
            int fat = recipe.getFat() > 0 ? recipe.getFat() : (int) (calories * 0.3 / 9); // 9 cal per gram
            
            // Set protein
            tvProteinValue.setText(protein + "g");
            int proteinPercent = Math.min(100, (protein * 100) / 50); // Daily value ~50g
            if (tvProteinPercent != null) tvProteinPercent.setText(proteinPercent + "%");
            if (progressProtein != null) progressProtein.setProgress(proteinPercent);
            
            // Set carbs
            tvCarbsValue.setText(carbs + "g");
            int carbsPercent = Math.min(100, (carbs * 100) / 300); // Daily value ~300g
            if (tvCarbsPercent != null) tvCarbsPercent.setText(carbsPercent + "%");
            if (progressCarbs != null) progressCarbs.setProgress(carbsPercent);
            
            // Set fat
            tvFatValue.setText(fat + "g");
            int fatPercent = Math.min(100, (fat * 100) / 65); // Daily value ~65g
            if (tvFatPercent != null) tvFatPercent.setText(fatPercent + "%");
            if (progressFat != null) progressFat.setProgress(fatPercent);
        } else {
            // Default values
            tvRecipeName.setText("Thông tin dinh dưỡng");
            tvCalorieValue.setText("0");
            tvProteinValue.setText("0g");
            tvCarbsValue.setText("0g");
            tvFatValue.setText("0g");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
