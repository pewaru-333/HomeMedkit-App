plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "ru.application.homemedkit"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.application.homemedkit"
        minSdk = 26
        targetSdk = 35
        versionCode = 55
        versionName = "1.8.0"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    androidResources {
        generateLocaleConfig = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ==================== Android ====================
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material3)

    // ==================== Room ====================
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // ==================== Network ====================
    implementation(libs.bundles.ktor)

    // ==================== Navigation ====================
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // ==================== Scanner ====================
    implementation(libs.bundles.camera)
    implementation(libs.zxing)

    // ==================== Settings ====================
    implementation(libs.material.preferences)

    // ==================== Coil ====================
    implementation(libs.coil.compose)
}