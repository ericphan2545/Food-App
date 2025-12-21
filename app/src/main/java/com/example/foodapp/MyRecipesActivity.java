package com.example.foodapp;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyRecipesActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ImageView btnDelete;
    private RecyclerView rvMyRecipes;
    private LinearLayout emptyState;
    private TextView tvEmptyMessage;

    private RecipeAdapter recipeAdapter;
    private List<Recipe> myRecipesList;
    private Map<Integer, String> recipeDocumentIds; // Map position to document ID
    
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

        btnBack.setOnClickListener(v -> {
            if (recipeAdapter.isSelectionMode()) {
                exitSelectionMode();
            } else {
                finish();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (recipeAdapter.isSelectionMode()) {
                deleteSelectedRecipes();
            } else {
                enterSelectionMode();
            }
        });
    }

    private void setupRecyclerView() {
        myRecipesList = new ArrayList<>();
        recipeDocumentIds = new HashMap<>();
        recipeAdapter = new RecipeAdapter(this, myRecipesList, recipe -> {
            if (!recipeAdapter.isSelectionMode()) {
                Intent intent = new Intent(MyRecipesActivity.this, RecipeDetailActivity.class);
                intent.putExtra("recipe", recipe);
                startActivity(intent);
            }
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
                    recipeDocumentIds.clear();
                    int position = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        myRecipesList.add(recipe);
                        recipeDocumentIds.put(position, document.getId());
                        position++;
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
        btnDelete.setImageResource(R.drawable.ic_delete_red);
        // Add subtle animation
        btnDelete.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .withEndAction(() -> btnDelete.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(200)
                        .start())
                .start();
    }

    private void exitSelectionMode() {
        recipeAdapter.setSelectionMode(false);
        btnDelete.setImageResource(R.drawable.ic_delete);
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
                    performDelete(selectedPositions);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performDelete(Set<Integer> selectedPositions) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> documentIdsToDelete = new ArrayList<>();
        for (Integer position : selectedPositions) {
            String docId = recipeDocumentIds.get(position);
            if (docId != null) {
                documentIdsToDelete.add(docId);
            }
        }

        if (documentIdsToDelete.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy món ăn để xóa", Toast.LENGTH_SHORT).show();
            exitSelectionMode();
            return;
        }

        // Delete from Firestore
        int deleteCount = documentIdsToDelete.size();
        int[] completedCount = {0};
        int[] successCount = {0};
        
        for (String docId : documentIdsToDelete) {
            db.collection("recipes")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        successCount[0]++;
                        completedCount[0]++;
                        if (completedCount[0] == deleteCount) {
                            if (successCount[0] > 0) {
                                Toast.makeText(this, "Đã xóa " + successCount[0] + " món ăn thành công", Toast.LENGTH_SHORT).show();
                            }
                            exitSelectionMode();
                            loadMyRecipes(); // Reload the list
                        }
                    })
                    .addOnFailureListener(e -> {
                        completedCount[0]++;
                        if (completedCount[0] == deleteCount) {
                            if (successCount[0] > 0) {
                                Toast.makeText(this, "Đã xóa " + successCount[0] + " món ăn", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Có lỗi xảy ra khi xóa", Toast.LENGTH_SHORT).show();
                            }
                            exitSelectionMode();
                            loadMyRecipes(); // Reload the list
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyRecipes();
    }
}

