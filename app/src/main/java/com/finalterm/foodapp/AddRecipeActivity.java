package com.example.foodapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRecipeActivity extends AppCompatActivity {

    private ImageView btnBack, ivRecipeImage;
    private EditText etRecipeName, etDescription, etTime, etServings, etCalories;
    private EditText etIngredients, etSteps;
    private Spinner spinnerType;
    private Button btnSelectImage, btnSaveRecipe;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        // Take persistable URI permission to avoid "Object does not exist" error
                        if (selectedImageUri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        selectedImageUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (SecurityException e) {
                                // Permission might not be persistable, that's okay
                                Log.d("AddRecipe", "Could not take persistable permission: " + e.getMessage());
                            }
                        }
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .into(ivRecipeImage);
                    }
                });

        initViews();
        setupSpinner();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        etRecipeName = findViewById(R.id.etRecipeName);
        etDescription = findViewById(R.id.etDescription);
        etTime = findViewById(R.id.etTime);
        etServings = findViewById(R.id.etServings);
        etCalories = findViewById(R.id.etCalories);
        etIngredients = findViewById(R.id.etIngredients);
        etSteps = findViewById(R.id.etSteps);
        spinnerType = findViewById(R.id.spinnerType);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveRecipe = findViewById(R.id.btnSaveRecipe);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupSpinner() {
        String[] types = {"Món chính", "Món nước", "Khai vị", "Tráng miệng"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSelectImage.setOnClickListener(v -> openImagePicker());

        ivRecipeImage.setOnClickListener(v -> openImagePicker());

        btnSaveRecipe.setOnClickListener(v -> saveRecipe());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private void saveRecipe() {
        String name = etRecipeName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String servings = etServings.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String ingredientsText = etIngredients.getText().toString().trim();
        String stepsText = etSteps.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();

        // Validation
        if (name.isEmpty()) {
            etRecipeName.setError("Vui lòng nhập tên món ăn");
            etRecipeName.requestFocus();
            return;
        }

        if (time.isEmpty()) {
            etTime.setError("Vui lòng nhập thời gian");
            etTime.requestFocus();
            return;
        }

        if (ingredientsText.isEmpty()) {
            etIngredients.setError("Vui lòng nhập nguyên liệu");
            etIngredients.requestFocus();
            return;
        }

        if (stepsText.isEmpty()) {
            etSteps.setError("Vui lòng nhập cách làm");
            etSteps.requestFocus();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm món ăn", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveRecipe.setEnabled(false);

        // Parse ingredients and steps
        List<String> ingredients = parseList(ingredientsText);
        List<String> steps = parseList(stepsText);
        int calories = caloriesStr.isEmpty() ? 0 : Integer.parseInt(caloriesStr);

        if (selectedImageUri != null) {
            // Upload image first
            uploadImageAndSaveRecipe(name, description, type, time, servings, calories, ingredients, steps, currentUser.getUid());
        } else {
            // Save recipe without image
            saveRecipeToFirestore(name, description, type, time, servings, calories, ingredients, steps, "", currentUser.getUid());
        }
    }

    private List<String> parseList(String text) {
        List<String> list = new ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                // Remove bullet points or numbers at the start
                trimmed = trimmed.replaceFirst("^[\\-\\*\\d\\.\\)]+\\s*", "");
                list.add(trimmed);
            }
        }
        return list;
    }

    private void uploadImageAndSaveRecipe(String name, String description, String type, 
                                          String time, String servings, int calories,
                                          List<String> ingredients, List<String> steps, String userId) {
        
        // Check if URI is still accessible
        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn lại ảnh", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnSaveRecipe.setEnabled(true);
            return;
        }

        // Convert image to Base64 in background thread
        new Thread(() -> {
            try {
                // Read image from URI
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) {
                    inputStream.close();
                }

                if (originalBitmap == null) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSaveRecipe.setEnabled(true);
                        Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Resize image to reduce size (max 400x400 for recipe image)
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, 400, 400);

                // Convert to Base64
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

                // Check size (Firestore document limit is 1MB, keep it under 800KB for safety)
                if (base64String.length() > 800000) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSaveRecipe.setEnabled(true);
                        Toast.makeText(this, "Ảnh quá lớn, vui lòng chọn ảnh nhỏ hơn", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Save to Firestore with Base64 image
                runOnUiThread(() -> {
                    saveRecipeToFirestore(name, description, type, time, servings, calories,
                            ingredients, steps, base64String, userId);
                });

            } catch (Exception e) {
                Log.e("AddRecipe", "Error processing image: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSaveRecipe.setEnabled(true);
                    Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void saveRecipeToFirestore(String name, String description, String type,
                                        String time, String servings, int calories,
                                        List<String> ingredients, List<String> steps,
                                        String imageBase64, String userId) {
        Map<String, Object> recipe = new HashMap<>();
        recipe.put("name", name);
        recipe.put("description", description);
        recipe.put("type", type);
        recipe.put("time", time + " phút");
        recipe.put("servings", servings.isEmpty() ? "1 người" : servings + " người");
        recipe.put("calories", calories);
        recipe.put("ingredients", ingredients);
        recipe.put("steps", steps);
        recipe.put("imageBase64", imageBase64); // Store as Base64 instead of URL
        recipe.put("userId", userId);
        recipe.put("createdAt", System.currentTimeMillis());

        db.collection("recipes")
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đã thêm món ăn thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSaveRecipe.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

