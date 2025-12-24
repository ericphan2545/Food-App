package com.example.foodapp.activities;

import com.example.foodapp.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.foodapp.models.Recipe;
import com.example.foodapp.adapters.RecipeAdapter;

public class MyRecipesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnDelete;
    private RecyclerView rvMyRecipes;
    private LinearLayout emptyState;
    private TextView tvEmptyMessage;
    private LinearLayout deleteActionBar;
    private TextView tvSelectedCount;
    private TextView btnCancelDelete;
    private TextView btnConfirmDelete;

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
        btnDelete = findViewById(R.id.btnDelete);
        rvMyRecipes = findViewById(R.id.rvMyRecipes);
        emptyState = findViewById(R.id.emptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        deleteActionBar = findViewById(R.id.deleteActionBar);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        btnCancelDelete = findViewById(R.id.btnCancelDelete);
        btnConfirmDelete = findViewById(R.id.btnConfirmDelete);

        btnBack.setOnClickListener(v -> {
            if (recipeAdapter.isSelectionMode()) {
                exitSelectionMode();
            } else {
                finish();
            }
        });

        btnDelete.setOnClickListener(v -> enterSelectionMode());

        btnCancelDelete.setOnClickListener(v -> exitSelectionMode());

        btnConfirmDelete.setOnClickListener(v -> deleteSelectedRecipes());
    }

    private void setupRecyclerView() {
        myRecipesList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, myRecipesList, recipe -> {
            if (!recipeAdapter.isSelectionMode()) {
                Intent intent = new Intent(MyRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra("recipe", recipe);
                startActivity(intent);
            }
        });

        recipeAdapter.setOnSelectionChangeListener(selectedCount -> {
            tvSelectedCount.setText("Đã chọn: " + selectedCount);
            btnConfirmDelete.setEnabled(selectedCount > 0);
            btnConfirmDelete.setAlpha(selectedCount > 0 ? 1.0f : 0.5f);
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
                        recipe.setDocumentId(document.getId()); // Lưu document ID để xóa
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

    private void enterSelectionMode() {
        recipeAdapter.setSelectionMode(true);
        deleteActionBar.setVisibility(View.VISIBLE);
        btnDelete.setVisibility(View.GONE);
    }

    private void exitSelectionMode() {
        recipeAdapter.setSelectionMode(false);
        recipeAdapter.clearSelection();
        deleteActionBar.setVisibility(View.GONE);
        btnDelete.setVisibility(View.VISIBLE);
    }

    private void deleteSelectedRecipes() {
        Set<Integer> selectedPositions = recipeAdapter.getSelectedPositions();
        if (selectedPositions.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn món ăn cần xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa " + selectedPositions.size() + " món ăn đã chọn?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    List<Recipe> recipesToDelete = new ArrayList<>();
                    for (Integer position : selectedPositions) {
                        if (position >= 0 && position < myRecipesList.size()) {
                            recipesToDelete.add(myRecipesList.get(position));
                        }
                    }

                    deleteRecipesFromFirestore(recipesToDelete);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteRecipesFromFirestore(List<Recipe> recipesToDelete) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalRecipes = recipesToDelete.size();
        final int[] successCount = {0};
        final int[] completedCount = {0};

        if (totalRecipes == 0) {
            return;
        }

        for (Recipe recipe : recipesToDelete) {
            String documentId = recipe.getDocumentId();
            if (documentId != null && !documentId.isEmpty()) {
                db.collection("recipes")
                        .document(documentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            successCount[0]++;
                            completedCount[0]++;
                            if (completedCount[0] == totalRecipes) {
                                String message = successCount[0] == totalRecipes 
                                    ? "Đã xóa " + successCount[0] + " món ăn" 
                                    : "Đã xóa " + successCount[0] + "/" + totalRecipes + " món ăn";
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                exitSelectionMode();
                                loadMyRecipes();
                            }
                        })
                        .addOnFailureListener(e -> {
                            completedCount[0]++;
                            if (completedCount[0] == totalRecipes) {
                                String message = successCount[0] > 0 
                                    ? "Đã xóa " + successCount[0] + "/" + totalRecipes + " món ăn" 
                                    : "Không thể xóa món ăn";
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                exitSelectionMode();
                                loadMyRecipes();
                            }
                        });
            } else {
                completedCount[0]++;
                if (completedCount[0] == totalRecipes) {
                    String message = successCount[0] > 0 
                        ? "Đã xóa " + successCount[0] + "/" + totalRecipes + " món ăn" 
                        : "Không thể xóa món ăn";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    exitSelectionMode();
                    loadMyRecipes();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyRecipes();
    }

    @Override
    public void onBackPressed() {
        if (recipeAdapter.isSelectionMode()) {
            exitSelectionMode();
        } else {
            super.onBackPressed();
        }
    }
}

