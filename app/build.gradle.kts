@file:Suppress("GradleDependency", "TomlDependencyVersion", "UseVersionCatalog", "AndroidLintGradleDependency", "DependencyUpdates")

import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.elothar.namagram"
    compileSdk = 34

    // 1. Enable BuildFeatures to generate the BuildConfig class
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.elothar.namagram"
        minSdk = 29
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 2. SAFELY READ ALL KEYS FROM LOCAL.PROPERTIES
        val properties = Properties().apply {
            val propertiesFile = rootProject.file("local.properties")
            if (propertiesFile.exists()) {
                load(FileInputStream(propertiesFile))
            }
        }

        val geminiKey = properties.getProperty("GEMINI_API_KEY") ?: ""
        val groqKey = properties.getProperty("GROQ_API_KEY") ?: ""
        val openRouterKey = properties.getProperty("OPENROUTER_API_KEY") ?: ""

        // 3. INJECT INTO BUILDCERT/BUILDCONFIG FIELDS
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
        buildConfigField("String", "GROQ_API_KEY", "\"$groqKey\"")
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$openRouterKey\"")

        // Keep manifest placeholder if you are still using it in AndroidManifest.xml
        manifestPlaceholders["GEMINI_API_KEY_VALUE"] = geminiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Enables R8 code shrinking & obfuscation
            isShrinkResources = false // Enables removal of unused resources
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

    androidResources {
        noCompress += "bin"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.mediapipe.genai)

    // Direct HTTP network driver
    implementation(libs.squareup.okhttp)

    // Lifecycle runtime
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}