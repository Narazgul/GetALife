buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.google.services)
        classpath(libs.firebase.appdistribution.gradle)
        classpath(libs.firebase.perf.plugin)
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.hilt.plugin) apply false
    alias(libs.plugins.ksp) apply false
}