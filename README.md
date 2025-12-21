# Food App - Thư viện món ăn

Ứng dụng Android hiển thị thư viện các món ăn với đầy đủ thông tin về nguyên liệu, cách chế biến và dinh dưỡng.

## Cấu trúc dự án

```
app/
├── src/main/
│   ├── java/com/example/foodapp/
│   │   ├── LoginActivity.java          # Màn hình đăng nhập
│   │   ├── MainActivity.java           # Màn hình chính
│   │   ├── RecipeDetailActivity.java   # Chi tiết món ăn (Tab Nguyên liệu)
│   │   ├── RecipeInstructionsActivity.java  # Chi tiết món ăn (Tab Cách chế biến)
│   │   ├── NutritionActivity.java      # Thông tin dinh dưỡng
│   │   ├── Recipe.java                 # Model Recipe
│   │   └── RecipeAdapter.java          # Adapter cho RecyclerView
│   │
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_login.xml
│   │   │   ├── activity_main.xml
│   │   │   ├── activity_recipe_detail.xml
│   │   │   ├── activity_recipe_instructions.xml
│   │   │   ├── activity_nutrition.xml
│   │   │   ├── item_recipe.xml
│   │   │   └── item_ingredient.xml
│   │   │
│   │   ├── drawable/                   # Icons và backgrounds
│   │   ├── values/
│   │   │   ├── colors.xml
│   │   │   ├── strings.xml
│   │   │   ├── themes.xml
│   │   │   └── dimens.xml
│   │   └── xml/
│   │
│   └── AndroidManifest.xml
│
├── build.gradle
└── proguard-rules.pro
```

## Các màn hình

### 1. Màn hình Đăng nhập (LoginActivity)
- Logo ứng dụng
- Trường nhập Email/Tên đăng nhập
- Trường nhập Mật khẩu với toggle hiển thị
- Nút Đăng nhập
- Link Quên mật khẩu và Đăng ký

### 2. Màn hình chính (MainActivity)
- Header với tiêu đề và avatar
- Thanh tìm kiếm với nút filter
- Category chips (Tất cả, Món chính, Tráng miệng, Khai vị)
- Grid hiển thị danh sách món ăn
- Bottom navigation (Trang chủ, Yêu thích, Thêm món, Cá nhân)

### 3. Màn hình Chi tiết món ăn - Tab Nguyên liệu (RecipeDetailActivity)
- Header với nút back, share, favorite
- Hình ảnh món ăn
- Tên và mô tả món ăn
- Tabs: Nguyên liệu | Cách chế biến | Dinh dưỡng
- Danh sách nguyên liệu với số lượng

### 4. Màn hình Chi tiết món ăn - Tab Cách chế biến (RecipeInstructionsActivity)
- Tương tự màn hình chi tiết
- Hiển thị các bước chế biến với số thứ tự

### 5. Màn hình Thông tin dinh dưỡng (NutritionActivity)
- Header với nút back
- Tên món ăn
- Vòng tròn hiển thị calories
- Thông tin Protein, Carbs, Chất béo
- Chi tiết dinh dưỡng (Cholesterol, Natri, Vitamin, ...)

## Cách sử dụng

1. Mở dự án trong Android Studio
2. Sync Gradle
3. Chạy ứng dụng trên emulator hoặc thiết bị thật

## Yêu cầu

- Android Studio Arctic Fox trở lên
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)

## Thư viện sử dụng

- AndroidX AppCompat
- Material Design Components
- ConstraintLayout
- RecyclerView
- CardView
- Glide (cho loading hình ảnh)

## Lưu ý

- Các hình ảnh món ăn hiện tại là placeholder. Bạn cần thay thế bằng hình ảnh thực tế.
- Để thêm hình ảnh thực, đặt file ảnh vào thư mục `res/drawable` và cập nhật trong code.

