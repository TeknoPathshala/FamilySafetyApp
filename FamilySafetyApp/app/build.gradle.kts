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
    // Latest Location API version (resolves legacy fragment & core-ui conflicts)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    
    // Explicit modern Core & Appcompat to override legacy transitive dependencies
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
