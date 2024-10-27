plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrainsKotlin)
    alias(libs.plugins.googleKsp)
    alias(libs.plugins.roomPlugin)
}

android {
    namespace = "ru.application.homemedkit"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.application.homemedkit"
        minSdk = 26
        targetSdk = 35
        versionCode = 39
        versionName = "1.5.4"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
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
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}

dependencies {
    // ==================== Android ====================
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material3)

    // ==================== BOMs ====================
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.kotlin.bom))

    // ==================== Room ====================
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // ==================== Retrofit ====================
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)

    // ==================== Navigation ====================
    ksp(libs.compose.destinations.ksp)
    implementation(libs.compose.destinations)

    // ==================== Scanner ====================
    implementation(libs.code.scanner)

    // ==================== Coil ====================
    implementation(libs.coil.compose)

    // ==================== Settings ====================
    implementation(libs.material.preferences)
}