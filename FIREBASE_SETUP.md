# Hướng dẫn cấu hình Firebase cho Food App

## Bước 1: Tạo Project Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" (Thêm dự án)
3. Đặt tên project: `FoodApp` (hoặc tên bạn muốn)
4. Chọn/bỏ chọn Google Analytics tùy ý
5. Click "Create project"

## Bước 2: Thêm ứng dụng Android

1. Trong Firebase Console, click biểu tượng Android
2. Điền thông tin:
   - **Package name**: `com.example.foodapp`
   - **App nickname**: Food App (tùy chọn)
   - **Debug signing certificate SHA-1**: (xem hướng dẫn bên dưới)
3. Click "Register app"

### Lấy SHA-1 Certificate:

Mở terminal trong Android Studio hoặc Command Prompt và chạy:

```bash
# Windows
cd C:\Users\YOUR_USERNAME\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android

# Mac/Linux
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Copy SHA-1 fingerprint và paste vào Firebase Console.

## Bước 3: Download và thêm google-services.json

1. Download file `google-services.json` từ Firebase Console
2. Copy file vào thư mục: `app/` (cùng cấp với `build.gradle`)

```
UIforapp/
├── app/
│   ├── google-services.json  <-- Đặt file ở đây
│   ├── build.gradle
│   └── src/
```

## Bước 4: Bật Authentication

1. Trong Firebase Console, vào **Authentication** > **Sign-in method**
2. Bật các phương thức:
   - **Email/Password**: Enable
   - **Google**: Enable (chọn support email)

## Bước 5: Cấu hình Firestore Database

1. Vào **Firestore Database** > **Create database**
2. Chọn **Start in test mode** (cho development)
3. Chọn location gần nhất (asia-southeast1 cho Việt Nam)

### Security Rules (cho production):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Favorites subcollection
      match /favorites/{recipeId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
    
    // Recipes collection
    match /recipes/{recipeId} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
        resource.data.userId == request.auth.uid;
    }
  }
}
```

## Bước 6: Cấu hình Firebase Storage

1. Vào **Storage** > **Get started**
2. Chọn **Start in test mode**

### Storage Rules (cho production):

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /recipes/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    match /profiles/{userId}/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## Bước 7: Cập nhật Web Client ID

1. Vào **Project Settings** > **General**
2. Scroll xuống **Your apps** > chọn Web app
3. Copy **Web client ID** từ OAuth 2.0 Client IDs
4. Mở file `app/src/main/res/values/strings.xml`
5. Thay thế `YOUR_WEB_CLIENT_ID` bằng ID thực:

```xml
<string name="default_web_client_id">YOUR_ACTUAL_WEB_CLIENT_ID</string>
```

## Bước 8: Sync và Build

1. Trong Android Studio, click **Sync Project with Gradle Files**
2. Build và chạy ứng dụng

## Troubleshooting

### Lỗi "Google Sign-In failed"
- Kiểm tra SHA-1 đã được thêm vào Firebase Console
- Kiểm tra `google-services.json` đã được đặt đúng vị trí
- Kiểm tra Web Client ID đã được cập nhật

### Lỗi "Network error"
- Đảm bảo có kết nối Internet
- Kiểm tra Firestore và Storage đã được enable

### Lỗi "Permission denied"
- Kiểm tra Security Rules trong Firestore/Storage
- Đảm bảo user đã đăng nhập trước khi thực hiện các thao tác

## Cấu trúc Database

### Collection: users
```
users/
  {userId}/
    fullName: string
    email: string
    photoUrl: string
    createdAt: timestamp
    favorites/
      {recipeId}/
        id: number
        name: string
        imageUrl: string
        type: string
        time: string
        servings: string
        calories: number
        ingredients: array
        steps: array
        addedAt: timestamp
```

### Collection: recipes
```
recipes/
  {recipeId}/
    name: string
    description: string
    imageUrl: string
    type: string
    time: string
    servings: string
    calories: number
    ingredients: array
    steps: array
    userId: string
    createdAt: timestamp
```

## Liên hệ hỗ trợ

Nếu gặp vấn đề, hãy kiểm tra:
- [Firebase Documentation](https://firebase.google.com/docs)
- [Android Firebase Guides](https://firebase.google.com/docs/android/setup)

