buildscript {
val kotlinVersion by extra("1.9.20")

    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-appdistribution-gradle:4.0.1")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
        classpath("com.google.firebase:perf-plugin:1.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
    repositories {
        mavenCentral()
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}