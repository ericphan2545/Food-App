package com.finalterm.foodapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView btnBack, btnShare, btnFavorite, ivRecipeImage;
    private TextView tvRecipeName, tvRecipeDescription;
    private TextView tvTime, tvServings, tvCalories;
    private TextView tabIngredients, tabInstructions, tabNutrition;
    private View tabIndicator1, tabIndicator2, tabIndicator3;
    private LinearLayout ingredientsContent, instructionsContent;
    
    private Recipe recipe;
    private boolean isFavorite = false;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        loadRecipeData();
        setupListeners();
        checkFavoriteStatus();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnFavorite = findViewById(R.id.btnFavorite);
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvRecipeDescription = findViewById(R.id.tvRecipeDescription);
        tvTime = findViewById(R.id.tvTime);
        tvServings = findViewById(R.id.tvServings);
        tvCalories = findViewById(R.id.tvCalories);
        tabIngredients = findViewById(R.id.tabIngredients);
        tabInstructions = findViewById(R.id.tabInstructions);
        tabNutrition = findViewById(R.id.tabNutrition);
        tabIndicator1 = findViewById(R.id.tabIndicator1);
        tabIndicator2 = findViewById(R.id.tabIndicator2);
        tabIndicator3 = findViewById(R.id.tabIndicator3);
        ingredientsContent = findViewById(R.id.ingredientsContent);
        instructionsContent = findViewById(R.id.instructionsContent);
    }

    private void loadRecipeData() {
        // Get recipe from intent
        recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        
        if (recipe != null) {
            tvRecipeName.setText(recipe.getName());
            
            // Set description
            if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
                tvRecipeDescription.setText(recipe.getDescription());
            } else {
                tvRecipeDescription.setText(generateDescription(recipe));
            }
            
            // Set time, servings, calories
            if (tvTime != null) tvTime.setText(recipe.getTime());
            if (tvServings != null) tvServings.setText(recipe.getServings());
            if (tvCalories != null) tvCalories.setText(recipe.getCalories() + " kcal");
            
            // Load image - priority: Base64 > URL > Resource
            if (recipe.getImageBase64() != null && !recipe.getImageBase64().isEmpty()) {
                // Load from Base64 (Firestore local storage)
                try {
                    byte[] decodedBytes = Base64.decode(recipe.getImageBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        ivRecipeImage.setImageBitmap(bitmap);
                    } else {
                        ivRecipeImage.setImageResource(R.drawable.img_placeholder);
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
            } else if (recipe.getImageResId() != 0) {
                ivRecipeImage.setImageResource(recipe.getImageResId());
            }
            
            // Load ingredients
            loadIngredients();
            
            // Load instructions
            loadInstructions();
        }
    }

    private String generateDescription(Recipe recipe) {
        if (recipe.getType() != null) {
            return "Một món " + recipe.getType().toLowerCase() + " ngon miệng, " +
                    "thời gian chuẩn bị " + recipe.getTime() + ", " +
                    "phù hợp cho " + recipe.getServings() + ".";
        }
        return "Một món ăn ngon miệng và dễ làm.";
    }

    private void loadIngredients() {
        ingredientsContent.removeAllViews();
        
        List<String> ingredients = recipe.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            for (int i = 0; i < ingredients.size(); i++) {
                View itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_ingredient, ingredientsContent, false);
                
                TextView tvNumber = itemView.findViewById(R.id.tvNumber);
                TextView tvIngredient = itemView.findViewById(R.id.tvIngredient);
                
                tvNumber.setText(String.valueOf(i + 1));
                tvIngredient.setText(ingredients.get(i));
                
                ingredientsContent.addView(itemView);
            }
        } else {
            TextView noData = new TextView(this);
            noData.setText("Chưa có thông tin nguyên liệu");
            noData.setTextColor(getResources().getColor(R.color.text_secondary));
            noData.setPadding(0, 16, 0, 16);
            ingredientsContent.addView(noData);
        }
    }

    private void loadInstructions() {
        if (instructionsContent == null) return;
        
        instructionsContent.removeAllViews();
        
        List<String> steps = recipe.getSteps();
        if (steps != null && !steps.isEmpty()) {
            for (int i = 0; i < steps.size(); i++) {
                View itemView = LayoutInflater.from(this)
                        .inflate(R.layout.item_step, instructionsContent, false);
                
                TextView tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
                TextView tvStepContent = itemView.findViewById(R.id.tvStepContent);
                
                tvStepNumber.setText("Bước " + (i + 1));
                tvStepContent.setText(steps.get(i));
                
                instructionsContent.addView(itemView);
            }
        } else {
            TextView noData = new TextView(this);
            noData.setText("Chưa có thông tin cách làm");
            noData.setTextColor(getResources().getColor(R.color.text_secondary));
            noData.setPadding(0, 16, 0, 16);
            instructionsContent.addView(noData);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareText = "Hãy thử món " + recipe.getName() + " này nhé!\n" +
                    "Thời gian: " + recipe.getTime() + "\n" +
                    "Calories: " + recipe.getCalories() + " kcal";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
        });

        btnFavorite.setOnClickListener(v -> {
            animateFavoriteButton();
            toggleFavorite();
        });

        tabIngredients.setOnClickListener(v -> selectTab(0));
        
        tabInstructions.setOnClickListener(v -> selectTab(1));
        
        tabNutrition.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeDetailActivity.this, NutritionActivity.class);
            intent.putExtra("recipe", recipe);
            startActivity(intent);
        });
    }

    private void selectTab(int tabIndex) {
        // Reset all tabs
        tabIngredients.setTextColor(getResources().getColor(R.color.text_secondary));
        tabIngredients.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabInstructions.setTextColor(getResources().getColor(R.color.text_secondary));
        tabInstructions.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabNutrition.setTextColor(getResources().getColor(R.color.text_secondary));
        tabNutrition.setTypeface(null, android.graphics.Typeface.NORMAL);

        tabIndicator1.setBackgroundColor(getResources().getColor(R.color.gray_light));
        tabIndicator2.setBackgroundColor(getResources().getColor(R.color.gray_light));
        tabIndicator3.setBackgroundColor(getResources().getColor(R.color.gray_light));

        // Hide all content
        ingredientsContent.setVisibility(View.GONE);
        if (instructionsContent != null) {
            instructionsContent.setVisibility(View.GONE);
        }

        // Set selected tab
        switch (tabIndex) {
            case 0:
                tabIngredients.setTextColor(getResources().getColor(R.color.text_primary));
                tabIngredients.setTypeface(null, android.graphics.Typeface.BOLD);
                tabIndicator1.setBackgroundColor(getResources().getColor(R.color.primary_green));
                ingredientsContent.setVisibility(View.VISIBLE);
                break;
            case 1:
                tabInstructions.setTextColor(getResources().getColor(R.color.text_primary));
                tabInstructions.setTypeface(null, android.graphics.Typeface.BOLD);
                tabIndicator2.setBackgroundColor(getResources().getColor(R.color.primary_green));
                if (instructionsContent != null) {
                    instructionsContent.setVisibility(View.VISIBLE);
                }
                break;
            case 2:
                tabNutrition.setTextColor(getResources().getColor(R.color.text_primary));
                tabNutrition.setTypeface(null, android.graphics.Typeface.BOLD);
                tabIndicator3.setBackgroundColor(getResources().getColor(R.color.primary_green));
                break;
        }
    }

    private void checkFavoriteStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || recipe == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .document(String.valueOf(recipe.getId()))
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFavorite = documentSnapshot.exists();
                    updateFavoriteIcon();
                });
    }

    private void toggleFavorite() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        if (recipe == null) return;

        String docId = String.valueOf(recipe.getId());
        if (docId.equals("0")) {
            docId = recipe.getName().replaceAll("[^a-zA-Z0-9]", "_");
        }

        if (isFavorite) {
            // Remove from favorites
            db.collection("users")
                    .document(currentUser.getUid())
                    .collection("favorites")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        updateFavoriteIcon();
                        playFavoriteAnimation();
                        Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add to favorites
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("id", recipe.getId());
            favoriteData.put("name", recipe.getName());
            favoriteData.put("imageUrl", recipe.getImageUrl());
            favoriteData.put("type", recipe.getType());
            favoriteData.put("time", recipe.getTime());
            favoriteData.put("servings", recipe.getServings());
            favoriteData.put("calories", recipe.getCalories());
            favoriteData.put("ingredients", recipe.getIngredients());
            favoriteData.put("steps", recipe.getSteps());
            favoriteData.put("addedAt", System.currentTimeMillis());

            db.collection("users")
                    .document(currentUser.getUid())
                    .collection("favorites")
                    .document(docId)
                    .set(favoriteData)
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        updateFavoriteIcon();
                        playFavoriteAnimation();
                        Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);
    }

    private void animateFavoriteButton() {
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        btnFavorite.startAnimation(bounceAnim);
    }

    private void playFavoriteAnimation() {
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        btnFavorite.startAnimation(scaleUp);
    }
}
