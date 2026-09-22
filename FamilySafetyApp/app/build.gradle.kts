plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Firebase Google Services Plugin
}

android {
    namespace = "com.example.familysafety"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.familysafety"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Google Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1") {
        exclude(group = "androidx.legacy")
    }

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")

    // Firebase BoM (Bill of Materials) - Sabhi Firebase libraries ke versions sync rakhta hai
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase Realtime Database (Live Location & Battery percentage sync ke liye)
    implementation("com.google.firebase:firebase-database-ktx")

    // Firebase Analytics (Optional)
    implementation("com.google.firebase:firebase-analytics-ktx")
}
