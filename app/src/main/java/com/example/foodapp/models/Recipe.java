package com.example.foodapp.models;

import com.google.gson.annotations.SerializedName; // 1. Import thư viện này
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recipe implements Serializable {

    // --- CÁC TRƯỜNG DỮ LIỆU TỪ API ---
    // Gson sẽ tìm key trong JSON trùng với @SerializedName để gán dữ liệu vào biến

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("imageUrl")
    private String imageUrl; // API trả về "imageUrl", biến này sẽ hứng link ảnh

    @SerializedName("type")
    private String type;

    @SerializedName("time")
    private String time;

    @SerializedName("servings")
    private String servings;

    @SerializedName("calories")
    private int calories;

    @SerializedName("ingredients")
    private List<String> ingredients;

    @SerializedName("steps")
    private List<String> steps;

    // --- CÁC TRƯỜNG CỦA RIÊNG APP BẠN (Firestore / Local) ---
    // (Giữ nguyên các trường này, khi gọi API chúng sẽ null hoặc bằng 0, không sao cả)
    private int imageResId; // Dùng cho ảnh nội bộ (R.drawable...)
    private boolean isFavorite;
    private String userId;
    private String description;
    private int protein;
    private int carbs;
    private int fat;
    
    @SerializedName("imageBase64")
    private String imageBase64; // Ảnh dạng Base64 cho Firestore (thay thế Firebase Storage)
    
    private String documentId; // Firestore document ID để xóa

    // --- CONSTRUCTORS ---

    // 1. Constructor rỗng (BẮT BUỘC cho Firestore và Gson)
    public Recipe() {
        this.ingredients = new ArrayList<>();
        this.steps = new ArrayList<>();
    }

    // 2. Constructor cho API và Firestore đầy đủ
    public Recipe(int id, String name, String imageUrl, String type, String time,
                  String servings, int calories, List<String> ingredients, List<String> steps) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type;
        this.time = time;
        this.servings = servings;
        this.calories = calories;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.steps = steps != null ? steps : new ArrayList<>();
    }

    // 3. Constructor cho dữ liệu local (nếu bạn vẫn dùng ảnh trong drawable)
    public Recipe(String name, String time, int imageResId) {
        this.name = name;
        this.time = time;
        this.imageResId = imageResId;
        this.ingredients = new ArrayList<>();
        this.steps = new ArrayList<>();
    }

    // --- GETTERS AND SETTERS (Giữ nguyên toàn bộ) ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getServings() { return servings; }
    public void setServings(String servings) { this.servings = servings; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getProtein() { return protein; }
    public void setProtein(int protein) { this.protein = protein; }

    public int getCarbs() { return carbs; }
    public void setCarbs(int carbs) { this.carbs = carbs; }

    public int getFat() { return fat; }
    public void setFat(int fat) { this.fat = fat; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
}