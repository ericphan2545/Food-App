package com.finalterm.foodapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CircleImageView ivProfileImage;
    private TextView tvUserName, tvUserEmail;
    private TextView tvRecipeCount, tvFavoriteCount;
    private LinearLayout btnEditProfile, btnMyRecipes, btnSettings, btnLogout;
    private LinearLayout navHome, navFavorites, navAdd, navProfile;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadProfileImage(imageUri);
                    }
                });

        initViews();
        setupListeners();
        loadUserData();
        loadStatistics();
        setupBottomNavigation();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvRecipeCount = findViewById(R.id.tvRecipeCount);
        tvFavoriteCount = findViewById(R.id.tvFavoriteCount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnMyRecipes = findViewById(R.id.btnMyRecipes);
        btnSettings = findViewById(R.id.btnSettings);
        btnLogout = findViewById(R.id.btnLogout);
        navHome = findViewById(R.id.navHome);
        navFavorites = findViewById(R.id.navFavorites);
        navAdd = findViewById(R.id.navAdd);
        navProfile = findViewById(R.id.navProfile);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        ivProfileImage.setOnClickListener(v -> openImagePicker());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnMyRecipes.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyRecipesActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserData() {
        if (currentUser != null) {
            tvUserName.setText(currentUser.getDisplayName() != null ? 
                    currentUser.getDisplayName() : "Người dùng");
            tvUserEmail.setText(currentUser.getEmail());

            // Load profile image from Firestore (Base64)
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Load fullName if available
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                tvUserName.setText(fullName);
                            }
                            
                            // Try to load Base64 photo first
                            String photoBase64 = documentSnapshot.getString("photoBase64");
                            if (photoBase64 != null && !photoBase64.isEmpty()) {
                                loadBase64Image(photoBase64);
                            } else if (currentUser.getPhotoUrl() != null) {
                                // Fallback to Firebase Auth photo URL
                                Glide.with(ProfileActivity.this)
                                        .load(currentUser.getPhotoUrl())
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .into(ivProfileImage);
                            }
                        } else if (currentUser.getPhotoUrl() != null) {
                            Glide.with(ProfileActivity.this)
                                    .load(currentUser.getPhotoUrl())
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .into(ivProfileImage);
                        }
                    });
        }
    }

    private void loadBase64Image(String base64String) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap != null) {
                ivProfileImage.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e("Profile", "Error loading Base64 image: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        if (currentUser == null) return;

        // Load recipe count
        db.collection("recipes")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvRecipeCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                });

        // Load favorite count
        db.collection("users")
                .document(currentUser.getUid())
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvFavoriteCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        if (currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                // Read image from URI
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) {
                    inputStream.close();
                }

                if (originalBitmap == null) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Resize image to reduce size (max 200x200 for profile picture)
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, 200, 200);

                // Convert to Base64
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

                // Check size (Firestore document limit is 1MB, keep it small)
                if (base64String.length() > 500000) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Ảnh quá lớn, vui lòng chọn ảnh nhỏ hơn", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Save to Firestore
                Map<String, Object> userData = new HashMap<>();
                userData.put("photoBase64", base64String);

                db.collection("users")
                        .document(currentUser.getUid())
                        .set(userData, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            // Update UI with new image
                            runOnUiThread(() -> {
                                loadBase64Image(base64String);
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ProfileActivity.this, "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .addOnFailureListener(e -> {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ProfileActivity.this, "Lỗi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        });

            } catch (Exception e) {
                Log.e("Profile", "Error uploading image: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private Bitmap resizeBitmap(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        navFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        navAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddRecipeActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            // Already on profile
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data in case it was updated in EditProfileActivity
        currentUser = mAuth.getCurrentUser();
        loadUserData();
        loadStatistics();
    }
}

