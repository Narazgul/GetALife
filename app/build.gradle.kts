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
}

android {
    namespace = "app.tinygiants.getalife"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.tinygiants.getalife"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "Ausbrühten 0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/hofi/AndroidStudioProjects/keystore")
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

            firebaseAppDistribution {
                artifactType = "APK"
                groups = "internal"
            }
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")

            manifestPlaceholders["appName"] = "Get A Life"
            buildConfigField(type = "String", name = "FIRESTORE_ROOT_COLLECTION", value = "\"release\"")

            firebaseAppDistribution {
                artifactType = "AAB"
                groups = "internal"
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
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

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.bundles.compose.debug)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(libs.bundles.hilt)
    ksp(libs.hilt.android.compiler)

    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.bundles.junit4)
}