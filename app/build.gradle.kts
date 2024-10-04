import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    id("com.google.devtools.ksp")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.dagger.hilt.android")
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "app.tinygiants.getalife"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.tinygiants.getalife"
        minSdk = 28
        targetSdk = 35
        versionCode = 6
        versionName = "Hatching 0.0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        buildConfigField(type = "String", name = "GEMINI_API_KEY", value = System.getenv("GEMINI_API_KEY"))
        buildConfigField(type = "String", name = "CHATGPT_API_KEY", value = System.getenv("CHATGPT_API_KEY"))
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/hofi/StudioProjects/keystore")
            storePassword = System.getenv("KEYSTORE_PW")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PW")
        }
    }

    firebaseAppDistribution {
        serviceCredentialsFile = "firebase/googleServiceCredentials.json"
        groups = "internal"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            manifestPlaceholders["appName"] = "Get A Life Debug"
            buildConfigField(type = "String", name = "FIRESTORE_ROOT_COLLECTION", value = "\"debug\"")

            firebaseAppDistribution { artifactType = "APK" }

            @Suppress("UnstableApiUsage")
            vcsInfo {
                include = true
            }
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")

            manifestPlaceholders["appName"] = "Get A Life"
            buildConfigField(type = "String", name = "FIRESTORE_ROOT_COLLECTION", value = "\"release\"")

            firebaseAppDistribution { artifactType = "AAB" }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.bundles.core)

    // AI
    implementation(libs.gemini.ai)
    implementation(libs.chatgpt.ai)

    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.emoji2.emojipicker)
    debugImplementation(libs.bundles.compose.debug)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // Hilt
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.bundles.room)
    testImplementation(libs.room.test)
    ksp(libs.room.compiler)

    // Junit
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.junit4)
}