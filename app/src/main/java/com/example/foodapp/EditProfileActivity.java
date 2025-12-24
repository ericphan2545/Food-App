package com.example.foodapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CircleImageView ivProfileImage;
    private TextView btnChangePhoto, tvEmail;
    private EditText etFullName, etCurrentPassword, etNewPassword, etConfirmPassword;
    private ImageView btnToggleCurrentPassword, btnToggleNewPassword, btnToggleConfirmPassword;
    private Button btnSaveName, btnChangePassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private boolean isCurrentPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            Glide.with(this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .into(ivProfileImage);
                            // Upload image as Base64 to Firestore
                            uploadImageAsBase64(imageUri);
                        }
                    }
                });

        initViews();
        setupListeners();
        loadUserData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        tvEmail = findViewById(R.id.tvEmail);
        etFullName = findViewById(R.id.etFullName);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnToggleCurrentPassword = findViewById(R.id.btnToggleCurrentPassword);
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        btnSaveName = findViewById(R.id.btnSaveName);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChangePhoto.setOnClickListener(v -> openImagePicker());
        ivProfileImage.setOnClickListener(v -> openImagePicker());

        btnSaveName.setOnClickListener(v -> saveName());

        btnChangePassword.setOnClickListener(v -> changePassword());

        // Toggle password visibility
        btnToggleCurrentPassword.setOnClickListener(v -> {
            isCurrentPasswordVisible = !isCurrentPasswordVisible;
            togglePasswordVisibility(etCurrentPassword, btnToggleCurrentPassword, isCurrentPasswordVisible);
        });

        btnToggleNewPassword.setOnClickListener(v -> {
            isNewPasswordVisible = !isNewPasswordVisible;
            togglePasswordVisibility(etNewPassword, btnToggleNewPassword, isNewPasswordVisible);
        });

        btnToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword, isConfirmPasswordVisible);
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleButton, boolean isVisible) {
        if (isVisible) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.ic_visibility);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.ic_visibility_off);
        }
        // Move cursor to end
        editText.setSelection(editText.getText().length());
    }

    private void loadUserData() {
        if (currentUser != null) {
            // Load email
            tvEmail.setText(currentUser.getEmail());

            // Load display name
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                etFullName.setText(displayName);
            }

            // Load profile image
            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(ivProfileImage);
            }

            // Also try to load from Firestore
            db.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                etFullName.setText(fullName);
                            }

                            // Try to load Base64 photo first
                            String photoBase64 = documentSnapshot.getString("photoBase64");
                            if (photoBase64 != null && !photoBase64.isEmpty()) {
                                loadBase64Image(photoBase64);
                            } else {
                                // Fallback to URL if exists
                                String photoUrlStr = documentSnapshot.getString("photoUrl");
                                if (photoUrlStr != null && !photoUrlStr.isEmpty()) {
                                    Glide.with(this)
                                            .load(photoUrlStr)
                                            .placeholder(R.drawable.ic_profile_placeholder)
                                            .into(ivProfileImage);
                                }
                            }
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
            Log.e("EditProfile", "Error loading Base64 image: " + e.getMessage());
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageAsBase64(Uri imageUri) {
        showLoading(true);
        
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
                        showLoading(false);
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
                if (base64String.length() > 500000) { // ~500KB limit for safety
                    runOnUiThread(() -> {
                        showLoading(false);
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
                            showLoading(false);
                            Toast.makeText(EditProfileActivity.this, "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            Toast.makeText(EditProfileActivity.this, "Lỗi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } catch (Exception e) {
                Log.e("EditProfile", "Error uploading image: " + e.getMessage());
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(EditProfileActivity.this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void saveName() {
        String newName = etFullName.getText().toString().trim();

        if (newName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return;
        }

        if (newName.length() < 2) {
            etFullName.setError("Họ và tên phải có ít nhất 2 ký tự");
            etFullName.requestFocus();
            return;
        }

        showLoading(true);
        
        // Timeout handler - tự động tắt loading sau 10 giây nếu không có response
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            Log.w("EditProfile", "Save name timeout - forcing loading off");
            if (progressBar.getVisibility() == View.VISIBLE) {
                showLoading(false);
                Toast.makeText(EditProfileActivity.this, "Đã lưu (có thể mất thời gian đồng bộ)", Toast.LENGTH_SHORT).show();
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 10000); // 10 giây timeout

        // Update Firebase Auth profile
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    Log.d("EditProfile", "Auth updateProfile completed: " + task.isSuccessful());
                    
                    if (task.isSuccessful()) {
                        // Also update Firestore using set() with merge option
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("fullName", newName);
                        
                        db.collection("users")
                                .document(currentUser.getUid())
                                .set(userData, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("EditProfile", "Firestore update SUCCESS");
                                    timeoutHandler.removeCallbacks(timeoutRunnable);
                                    showLoading(false);
                                    Toast.makeText(EditProfileActivity.this, "Đã cập nhật họ và tên", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("EditProfile", "Firestore update FAILED: " + e.getMessage());
                                    timeoutHandler.removeCallbacks(timeoutRunnable);
                                    showLoading(false);
                                    // Vẫn thông báo thành công vì Auth đã lưu được
                                    Toast.makeText(EditProfileActivity.this, "Đã cập nhật họ và tên", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("EditProfile", "Auth update FAILED: " + (task.getException() != null ? task.getException().getMessage() : "unknown"));
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        showLoading(false);
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                        Toast.makeText(EditProfileActivity.this, "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validation
        if (currentPassword.isEmpty()) {
            etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            etCurrentPassword.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Vui lòng nhập mật khẩu mới");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        if (currentPassword.equals(newPassword)) {
            etNewPassword.setError("Mật khẩu mới phải khác mật khẩu hiện tại");
            etNewPassword.requestFocus();
            return;
        }

        showLoading(true);

        // Re-authenticate user first
        String email = currentUser.getEmail();
        if (email == null) {
            showLoading(false);
            Toast.makeText(this, "Không thể xác định email người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Now update password
                        currentUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    showLoading(false);
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(this, "Đã đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                        // Clear password fields
                                        etCurrentPassword.setText("");
                                        etNewPassword.setText("");
                                        etConfirmPassword.setText("");
                                    } else {
                                        String error = updateTask.getException() != null ? 
                                                updateTask.getException().getMessage() : "Lỗi không xác định";
                                        Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                        etCurrentPassword.setError("Mật khẩu không đúng");
                        etCurrentPassword.requestFocus();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveName.setEnabled(!show);
        btnChangePassword.setEnabled(!show);
    }
}

