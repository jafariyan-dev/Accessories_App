plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
//    alias(libs.plugins.google.gms.google.services)
    }

android {
    namespace = "com.example.accessories_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.accessories_app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation (libs.retrofit)
    implementation (libs.com.squareup.retrofit2.converter.scalars5)
    implementation (libs.com.squareup.okhttp3.okhttp4)
    implementation (libs.androidx.cardview)
    implementation (libs.androidx.exifinterface)
    implementation (libs.android.gif.drawable)
    implementation (libs.sdp.android)
    implementation (libs.converter.gson.v290)
    // کتابخانه‌ها
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase Database
    implementation(libs.firebase.database.v2021)
    implementation(libs.firebase.database.ktx.v2021)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.glide.v4160)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.ui)
    implementation(libs.material3)
    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.ui.tooling)
    implementation(libs.androidx.savedstate.ktx)
    implementation(libs.androidx.lifecycle.common.java8)
}
