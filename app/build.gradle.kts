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

    // Core AndroidX & UI Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.core:core-ktx:1.12.0") // KTX for Kotlin extensions
    implementation("androidx.recyclerview:recyclerview:1.3.2") // For displaying lists
    implementation("androidx.cardview:cardview:1.0.0") // For card-based layouts

    // CircleImageView for profile pictures
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Firebase - Using the Bill of Materials (BOM) to manage versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Glide for image loading from URLs
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Retrofit for network API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Gson for JSON parsing
    implementation(libs.gson) // Already had this, keeping it

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}