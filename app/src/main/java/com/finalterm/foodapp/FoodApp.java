package com.example.foodapp;

import android.app.Application;

public class FoodApp extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Apply saved theme on app start
        SettingsActivity.applyTheme(this);
    }
}

