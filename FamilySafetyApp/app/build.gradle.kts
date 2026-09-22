plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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

    // AndroidX Core for compatibility
    implementation("androidx.core:core-ktx:1.12.0")
}
