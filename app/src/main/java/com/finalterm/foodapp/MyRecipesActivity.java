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

public class MyRecipesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvMyRecipes;
    private LinearLayout emptyState;
    private TextView tvEmptyMessage;

    private RecipeAdapter recipeAdapter;
    private List<Recipe> myRecipesList;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadMyRecipes();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvMyRecipes = findViewById(R.id.rvMyRecipes);
        emptyState = findViewById(R.id.emptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        myRecipesList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, myRecipesList, recipe -> {
            Intent intent = new Intent(MyRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe", recipe);
            startActivity(intent);
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvMyRecipes.setLayoutManager(layoutManager);
        rvMyRecipes.setAdapter(recipeAdapter);
    }

    private void loadMyRecipes() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showEmptyState("Vui lòng đăng nhập để xem món ăn của bạn");
            return;
        }

        db.collection("recipes")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    myRecipesList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        myRecipesList.add(recipe);
                    }
                    
                    if (myRecipesList.isEmpty()) {
                        showEmptyState("Bạn chưa tạo món ăn nào.\nHãy thêm món ăn đầu tiên!");
                    } else {
                        emptyState.setVisibility(View.GONE);
                        rvMyRecipes.setVisibility(View.VISIBLE);
                    }
                    recipeAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    showEmptyState("Không thể tải danh sách món ăn");
                });
    }

    private void showEmptyState(String message) {
        emptyState.setVisibility(View.VISIBLE);
        rvMyRecipes.setVisibility(View.GONE);
        tvEmptyMessage.setText(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyRecipes();
    }
}

