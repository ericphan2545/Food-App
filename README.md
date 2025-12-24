# Food App (UIforapp)

Ứng dụng Android hiển thị và quản lý món ăn: xem danh sách, chi tiết, dinh dưỡng, thêm món, và quản lý món của tôi (có chọn/xóa nhiều).

## Cấu trúc thư mục chính
```
app/
├── src/main/
│   ├── java/com/example/foodapp/
│   │   ├── LoginActivity.java / RegisterActivity.java / ForgotPasswordActivity.java
│   │   ├── MainActivity.java / RecipeDetailActivity.java / RecipeInstructionsActivity.java
│   │   ├── NutritionActivity.java / FavoritesActivity.java / ProfileActivity.java / SettingsActivity.java
│   │   ├── AddRecipeActivity.java / MyRecipesActivity.java
│   │   ├── RecipeAdapter.java / RecipeApiService.java
│   │   └── Recipe.java (model)
│   │
│   ├── res/
│   │   ├── layout/ (các màn hình activity_*.xml, item_recipe.xml, item_step.xml, item_ingredient.xml)
│   │   ├── drawable/ (icon, background, checkbox_background, ic_delete, ...)
│   │   ├── values/ (colors.xml, strings.xml, themes.xml, dimens.xml)
│   │   └── assets/recipes_data.json
│   └── AndroidManifest.xml
├── build.gradle
└── proguard-rules.pro
```

## Tính năng chính
- Đăng nhập/Đăng ký/Quên mật khẩu với Firebase Auth.
- Trang chủ: tìm kiếm, lọc, xem danh sách món ăn, chi tiết món ăn, xem bước nấu và dinh dưỡng.
- Thêm món mới (AddRecipeActivity) lưu Firestore (ảnh base64 trong `imageBase64`).
- Món ăn của tôi (MyRecipesActivity): xem món của user, chế độ chọn nhiều với checkbox, xóa nhiều món (Firestore).
- Yêu thích, hồ sơ cá nhân, cài đặt giao diện (light/dark).

## Cách chạy
1. Mở dự án trong Android Studio.
2. Đặt file `app/google-services.json` (Firebase) phù hợp dự án của bạn.
3. Sync Gradle.
4. Chạy trên emulator hoặc thiết bị thật.

## Yêu cầu môi trường
- Android Studio Arctic Fox trở lên.
- Min SDK 24, Target SDK 34.

## Thư viện/chính
- AndroidX AppCompat, Material Components, ConstraintLayout, RecyclerView, CardView.
- Glide (load ảnh), Firebase Auth + Firestore.

## Ghi chú
- Ảnh mẫu là placeholder; thay bằng ảnh thật nếu cần.
- Build artifacts đã được loại khỏi repo qua `.gitignore`.

