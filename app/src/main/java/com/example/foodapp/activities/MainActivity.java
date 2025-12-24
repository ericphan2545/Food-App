package com.example.foodapp.activities;

import com.example.foodapp.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast; // Import thêm Toast để báo lỗi nếu cần

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

// Import Retrofit
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

import com.example.foodapp.models.Recipe;
import com.example.foodapp.adapters.RecipeAdapter;
import com.example.foodapp.services.RecipeApiService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText etSearch;
    private ImageView ivProfile;
    private TextView chipAll, chipMainDish, chipDessert, chipAppetizer, chipSoup;
    private RecyclerView rvRecipes;
    private LinearLayout navHome, navFavorites, navAdd, navProfile;

    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;
    private List<Recipe> allRecipes;
    private String currentFilter = "Tất cả";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupChips();
        setupBottomNavigation();
        setupSearch();

        // Gọi hàm load dữ liệu
        loadRecipes();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        ivProfile = findViewById(R.id.ivProfile);
        chipAll = findViewById(R.id.chipAll);
        chipMainDish = findViewById(R.id.chipMainDish);
        chipDessert = findViewById(R.id.chipDessert);
        chipAppetizer = findViewById(R.id.chipAppetizer);
        chipSoup = findViewById(R.id.chipSoup);
        rvRecipes = findViewById(R.id.rvRecipes);
        navHome = findViewById(R.id.navHome);
        navFavorites = findViewById(R.id.navFavorites);
        navAdd = findViewById(R.id.navAdd);
        navProfile = findViewById(R.id.navProfile);

        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        recipeList = new ArrayList<>();
        allRecipes = new ArrayList<>();

        // Lưu ý: Đảm bảo RecipeAdapter của bạn dùng Glide để load ảnh từ URL (imageUrl)
        recipeAdapter = new RecipeAdapter(this, recipeList, recipe -> {
            Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe", recipe);
            startActivity(intent);
        });

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvRecipes.setLayoutManager(layoutManager);
        rvRecipes.setAdapter(recipeAdapter);
    }

    private void loadRecipes() {
        // 1. Load từ API thay vì file JSON local
        loadApiRecipes();

        // 2. Load thêm từ Firestore (nếu có tính năng user tự tạo món)
        loadFirestoreRecipes();
    }

    // --- HÀM MỚI: LOAD API ---
    private void loadApiRecipes() {
        // Cấu hình Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://692d9d81e5f67cd80a4c3fa9.mockapi.io/foodib/api/v1/") // Base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Tạo service
        RecipeApiService service = retrofit.create(RecipeApiService.class);

        // Gọi API
        service.getRecipes().enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Recipe> apiRecipes = response.body();

                    Log.d(TAG, "API Loaded: " + apiRecipes.size() + " recipes");

                    // Thêm dữ liệu vào danh sách tổng
                    allRecipes.addAll(apiRecipes);

                    // Cập nhật giao diện (qua hàm filter để áp dụng cả tìm kiếm/lọc nếu có)
                    filterRecipes();
                } else {
                    Log.e(TAG, "API Error: " + response.code());
                    Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Log.e(TAG, "API Failure: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();

                // Nếu lỗi mạng, có thể gọi loadSampleRecipes() để chữa cháy nếu muốn
                // loadSampleRecipes();
            }
        });
    }

    // Giữ nguyên hàm load từ Firestore (nếu bạn vẫn muốn dùng)
    private void loadFirestoreRecipes() {
        db.collection("recipes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Recipe recipe = document.toObject(Recipe.class);
                        // Tránh trùng lặp (nếu tên giống nhau)
                        boolean exists = false;
                        for (Recipe r : allRecipes) {
                            if (r.getName() != null && r.getName().equals(recipe.getName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            allRecipes.add(recipe);
                        }
                    }
                    filterRecipes();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading Firestore recipes: " + e.getMessage());
                });
    }

    // Bạn có thể xóa hàm loadSampleRecipes nếu không dùng nữa,
    // hoặc giữ lại để làm dữ liệu dự phòng (backup) khi mất mạng.

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecipes();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterRecipes() {
        String searchQuery = etSearch.getText().toString().toLowerCase().trim();

        recipeList.clear();
        for (Recipe recipe : allRecipes) {
            // Kiểm tra null để tránh crash app
            String type = recipe.getType() != null ? recipe.getType() : "";
            String name = recipe.getName() != null ? recipe.getName().toLowerCase() : "";

            boolean matchesFilter = currentFilter.equals("Tất cả") || type.equals(currentFilter);
            boolean matchesSearch = searchQuery.isEmpty() || name.contains(searchQuery);

            if (matchesFilter && matchesSearch) {
                recipeList.add(recipe);
            }
        }
        recipeAdapter.notifyDataSetChanged();
    }

    private void setupChips() {
        View.OnClickListener chipClickListener = v -> {
            resetChips();

            TextView selectedChip = (TextView) v;
            selectedChip.setBackgroundResource(R.drawable.bg_chip_selected);
            selectedChip.setTextColor(getResources().getColor(R.color.white));

            currentFilter = selectedChip.getText().toString();
            filterRecipes();
        };

        chipAll.setOnClickListener(chipClickListener);
        chipMainDish.setOnClickListener(chipClickListener);
        chipDessert.setOnClickListener(chipClickListener);
        chipAppetizer.setOnClickListener(chipClickListener);
        if (chipSoup != null) {
            chipSoup.setOnClickListener(chipClickListener);
        }
    }

    private void resetChips() {
        int unselectedColor = getResources().getColor(R.color.text_primary); // Đảm bảo màu này có trong colors.xml

        // Bạn có thể dùng ContextCompat để lấy màu an toàn hơn
        // int unselectedColor = ContextCompat.getColor(this, R.color.text_primary);

        chipAll.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipAll.setTextColor(unselectedColor);
        chipMainDish.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipMainDish.setTextColor(unselectedColor);
        chipDessert.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipDessert.setTextColor(unselectedColor);
        chipAppetizer.setBackgroundResource(R.drawable.bg_chip_unselected);
        chipAppetizer.setTextColor(unselectedColor);
        if (chipSoup != null) {
            chipSoup.setBackgroundResource(R.drawable.bg_chip_unselected);
            chipSoup.setTextColor(unselectedColor);
        }
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            // Already on home
        });

        navFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        navAdd.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Intent intent = new Intent(MainActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh recipes when returning to this activity
        // Lưu ý: Gọi API nhiều lần trong onResume có thể gây tốn dung lượng
        // Có thể cân nhắc chỉ gọi loadFirestoreRecipes() tại đây
    }
}