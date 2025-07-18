plugins {
    id("com.google.devtools.ksp")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.junit5.plugin)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "app.tinygiants.getalife"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.tinygiants.getalife"
        minSdk = 28
        targetSdk = 36
        versionCode = 13
        versionName = "Hatching 0.0.13"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        buildConfigField(type = "String", name = "CHATGPT_API_KEY", value = System.getenv("CHATGPT_API_KEY"))
        buildConfigField(type = "String", name = "SUPERWALL_PUBLIC_KEY", value = System.getenv("SUPERWALL_PUBLIC_KEY"))
        buildConfigField(type = "String", name = "REVENUECAT_API_KEY", value = System.getenv("REVENUECAT_API_KEY"))
        buildConfigField(type = "String", name = "CRISP_CHAT", value = System.getenv("CRISP_CHAT"))
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH"))
            storePassword = System.getenv("KEYSTORE_PW")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PW")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            manifestPlaceholders["appName"] = "Get A Life Debug"
            buildConfigField(type = "String", name = "FIRESTORE_ROOT_COLLECTION", value = "\"debug\"")

            @Suppress("UnstableApiUsage")
            vcsInfo { include = true }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")

            manifestPlaceholders["appName"] = "Get A Life"
            buildConfigField(type = "String", name = "FIRESTORE_ROOT_COLLECTION", value = "\"release\"")
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

// Set Locale to en-US for unit tests
// Ensures assertion and expectation use similar formatting
tasks {
    withType<Test> {
        systemProperty("user.language", "en")
        systemProperty("user.country", "US")
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

dependencies {
    // Core
    implementation(libs.bundles.core)

    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.storage)
    debugImplementation(libs.bundles.compose.debug)

    // Room
    implementation(libs.bundles.room)
    testImplementation(libs.room.test)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // AI
    implementation(libs.firebase.ai)
    implementation(libs.chatgpt.ai)

    // Misc
    implementation(libs.emoji2.emojipicker)
    implementation(libs.bundles.superwall)
    implementation(libs.revenuecat)
    implementation(libs.crisp)

    // Testing
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.testing.android)
}