package com.finalterm.foodapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FoodAppSettings";
    private static final String KEY_THEME_MODE = "theme_mode";

    // Theme mode values
    private static final int THEME_SYSTEM = 0;
    private static final int THEME_LIGHT = 1;
    private static final int THEME_DARK = 2;

    private ImageView btnBack, ivThemeIcon;
    private SwitchCompat switchDarkMode;
    private TextView tvThemeStatus;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivThemeIcon = findViewById(R.id.ivThemeIcon);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        tvThemeStatus = findViewById(R.id.tvThemeStatus);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setThemeMode(THEME_DARK);
            } else {
                setThemeMode(THEME_LIGHT);
            }
        });
    }

    private void updateUI() {
        int currentMode = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM);

        switch (currentMode) {
            case THEME_LIGHT:
                switchDarkMode.setChecked(false);
                tvThemeStatus.setText("Đang tắt");
                ivThemeIcon.setImageResource(R.drawable.ic_light_mode);
                break;
            case THEME_DARK:
                switchDarkMode.setChecked(true);
                tvThemeStatus.setText("Đang bật");
                ivThemeIcon.setImageResource(R.drawable.ic_dark_mode);
                break;
            case THEME_SYSTEM:
            default:
                // Check current system theme
                int nightMode = getResources().getConfiguration().uiMode 
                        & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
                boolean isNightMode = nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
                switchDarkMode.setChecked(isNightMode);
                tvThemeStatus.setText(isNightMode ? "Đang bật" : "Đang tắt");
                ivThemeIcon.setImageResource(isNightMode ? R.drawable.ic_dark_mode : R.drawable.ic_light_mode);
                break;
        }
    }

    private void setThemeMode(int mode) {
        // Save preference
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();

        // Apply theme
        switch (mode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        // Update UI will be called automatically when activity recreates
    }

    /**
     * Call this method from Application class or splash screen to apply saved theme on app start
     */
    public static void applyTheme(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int mode = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM);

        switch (mode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}

