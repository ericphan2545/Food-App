package com.finalterm.foodapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvFavorites;
    private LinearLayout emptyState;
    private TextView tvEmptyMessage;
    private LinearLayout navHome, navFavorites, navAdd, navProfile;

    private RecipeAdapter recipeAdapter;
    private List<Recipe> favoritesList;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        loadFavorites();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvFavorites = findViewById(R.id.rvFavorites);
        emptyState = findViewById(R.id.emptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        navHome = findViewById(R.id.navHome);
        navFavorites = findViewById(R.id.navFavorites);
        navAdd = findViewById(R.id.navAdd);
        navProfile = findViewById(R.id.navProfile);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        favoritesList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, favoritesList, recipe -> {
            Intent intent = new Intent(FavoritesActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe", recipe);
            startActivity(intent);
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvFavorites.setLayoutManager(layoutManager);
        rvFavorites.setAdapter(recipeAdapter);
    }

    private void loadFavorites() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showEmptyState("Vui lòng đăng nhập để xem danh sách yêu thích");
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoritesList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setFavorite(true);
                        favoritesList.add(recipe);
                    }
                    
                    if (favoritesList.isEmpty()) {
                        showEmptyState("Bạn chưa có món ăn yêu thích nào.\nHãy khám phá và thêm món ăn vào danh sách!");
                    } else {
                        emptyState.setVisibility(View.GONE);
                        rvFavorites.setVisibility(View.VISIBLE);
                    }
                    recipeAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    showEmptyState("Không thể tải danh sách yêu thích");
                });
    }

    private void showEmptyState(String message) {
        emptyState.setVisibility(View.VISIBLE);
        rvFavorites.setVisibility(View.GONE);
        tvEmptyMessage.setText(message);
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navFavorites.setOnClickListener(v -> {
            // Already on favorites
        });

        navAdd.setOnClickListener(v -> {
            Intent intent = new Intent(FavoritesActivity.this, AddRecipeActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FavoritesActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }
}

