package com.example.foodapp.activities;

import com.example.foodapp.R;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etEmail;
    private Button btnResetPassword;
    private TextView tvMessage;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvMessage = findViewById(R.id.tvMessage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnResetPassword.setEnabled(true);

                    if (task.isSuccessful()) {
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Đã gửi email khôi phục mật khẩu.\nVui lòng kiểm tra hộp thư của bạn.");
                        tvMessage.setTextColor(getResources().getColor(R.color.primary_green));
                        Toast.makeText(ForgotPasswordActivity.this, 
                                "Email khôi phục đã được gửi!", Toast.LENGTH_LONG).show();
                    } else {
                        tvMessage.setVisibility(View.VISIBLE);
                        String errorMessage = "Không thể gửi email khôi phục";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error != null && error.contains("no user record")) {
                                errorMessage = "Email này chưa được đăng ký";
                            }
                        }
                        tvMessage.setText(errorMessage);
                        tvMessage.setTextColor(getResources().getColor(R.color.heart_red));
                    }
                });
    }
}

