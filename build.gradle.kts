// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3")
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.44")
    }
}

plugins {
    id("com.android.application") version "7.3.1" apply false
    id("com.android.library") version "7.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20" apply false
    id("com.google.firebase.crashlytics") version "2.9.7" apply false
}
