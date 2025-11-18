// Top-level build file where you can add configuration options common to all sub-projects/modules.

// Jetpack Compose Plugin Configuration
// Using the modern Kotlin Compose Compiler Plugin (Kotlin 2.0+)
// This plugin is available from:
// - Gradle Plugin Portal: https://plugins.gradle.org/
// - Maven Central: for library artifacts
// - Google Maven: for AndroidX Compose libraries (configured in settings.gradle.kts)

plugins {
    // Android Gradle Plugin for building Android applications
    id("com.android.application") version "8.3.0" apply false
    
    // Kotlin Android plugin for Kotlin support
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    
    // Compose Compiler Plugin (Kotlin 2.0+)
    // Replaces the need for manual kotlinCompilerExtensionVersion configuration
    // Automatically manages Compose compiler version compatibility
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}