// Plugin Management Configuration
// Defines repositories for Gradle plugins
pluginManagement {
    repositories {
        // Google Maven Repository - Required for Android Gradle Plugin and AndroidX libraries
        // Contains: com.android.tools.build:gradle, AndroidX Compose artifacts
        google()
        
        // Maven Central - Contains most Kotlin and JetBrains plugins
        // Contains: Kotlin plugins, Compose Multiplatform artifacts
        mavenCentral()
        
        // Gradle Plugin Portal - Official Gradle plugin repository
        // Contains: Kotlin Compose Compiler Plugin, other community plugins
        gradlePluginPortal()
    }
}

// Dependency Resolution Management
// Defines repositories for project dependencies
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Google Maven Repository - Required for AndroidX Compose libraries
        // URL: https://maven.google.com
        // Contains: androidx.compose.ui:ui, androidx.compose.material3:material3, etc.
        google()
        
        // Maven Central - Contains Kotlin standard library and other dependencies
        // Contains: Supabase client libraries, Ktor, Coroutines, etc.
        mavenCentral()
    }
}

rootProject.name = "SupabaseProductos"
include(":app")
 