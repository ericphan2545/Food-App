plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.finalterm.foodapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.finalterm.foodapp"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // CircleImageView for rounded avatar with optional border attributes (app:civ_border_width, app:civ_border_color)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // Gson for JSON serialization/deserialization (SerializedName)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}