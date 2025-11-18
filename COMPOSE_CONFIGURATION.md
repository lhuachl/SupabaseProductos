# Jetpack Compose Configuration Guide

This document explains the Jetpack Compose configuration in this project and how it differs from older setups.

## Modern Configuration (Kotlin 2.0+)

This project uses **Kotlin 2.0.21** with the **Compose Compiler Plugin**, which is the modern and recommended approach.

### Root build.gradle.kts

```kotlin
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
```

### Module build.gradle.kts (app/build.gradle.kts)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")  // Modern Compose plugin
}

android {
    buildFeatures {
        compose = true  // Enable Jetpack Compose
    }
    
    // No composeOptions block needed!
    // The plugin handles this automatically
}

dependencies {
    // Using BOM for version management
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    // ... other Compose dependencies
}
```

## Legacy Configuration (Kotlin <2.0)

For reference, here's how Compose was configured in older projects:

### build.gradle.kts (Legacy)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // No Compose plugin needed
}

android {
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"  // Manual version management
    }
}

dependencies {
    // Manual version management
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.compiler:compiler:1.5.10")
}
```

## Key Differences

| Feature | Modern (Kotlin 2.0+) | Legacy (Kotlin <2.0) |
|---------|---------------------|----------------------|
| Compose Plugin | `org.jetbrains.kotlin.plugin.compose` | Not needed |
| composeOptions | Not needed | Manual `kotlinCompilerExtensionVersion` |
| Compiler Version | Auto-managed by plugin | Manual dependency |
| Version Compatibility | Automatically handled | Manual coordination needed |

## Repository Configuration

### settings.gradle.kts

```kotlin
pluginManagement {
    repositories {
        google()           // Android Gradle Plugin
        mavenCentral()     // Kotlin plugins
        gradlePluginPortal() // Compose Compiler Plugin
    }
}

dependencyResolutionManagement {
    repositories {
        google()        // AndroidX Compose libraries
        mavenCentral()  // Other dependencies
    }
}
```

## Available Repositories

1. **Gradle Plugin Portal** (`gradlePluginPortal()`)
   - Contains: `org.jetbrains.kotlin.plugin.compose`
   - URL: https://plugins.gradle.org/

2. **Maven Central** (`mavenCentral()`)
   - Contains: Compose Multiplatform artifacts, Kotlin stdlib
   - URL: https://repo1.maven.org/maven2/

3. **Google Maven** (`google()`)
   - Contains: AndroidX Compose libraries, AGP
   - URL: https://maven.google.com
   - **Required** for all Android projects

## Dependencies Overview

This project uses these Compose-related dependencies:

```kotlin
// Core Compose dependencies (via BOM)
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.ui)
implementation(libs.androidx.ui.graphics)
implementation(libs.androidx.ui.tooling.preview)
implementation(libs.androidx.material3)

// Compose integration libraries
implementation(libs.androidx.activity.compose)
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
implementation("androidx.navigation:navigation-compose:2.8.4")

// Debug tools
debugImplementation(libs.androidx.ui.tooling)
debugImplementation(libs.androidx.ui.test.manifest)

// Testing
androidTestImplementation(platform(libs.androidx.compose.bom))
androidTestImplementation(libs.androidx.ui.test.junit4)
```

## Version Catalog (gradle/libs.versions.toml)

```toml
[versions]
agp = "8.3.0"
kotlin = "2.0.21"
composeBom = "2024.09.00"

[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

## Benefits of Modern Configuration

1. **Automatic Version Management**: No need to manually coordinate compiler versions
2. **Simplified Configuration**: Less boilerplate in build files
3. **Better Compatibility**: Plugin ensures versions work together
4. **Future-Proof**: Aligned with Kotlin's roadmap
5. **Multiplatform Ready**: Same plugin works for Compose Multiplatform

## Migration from Legacy

If migrating from older configuration:

1. Update Kotlin to 2.0+
2. Add `org.jetbrains.kotlin.plugin.compose` plugin
3. Remove `composeOptions` block
4. Remove explicit `androidx.compose.compiler:compiler` dependency
5. Update to latest Compose BOM

## Troubleshooting

### Build fails with "plugin not found"

Ensure `gradlePluginPortal()` is in `pluginManagement` repositories in `settings.gradle.kts`.

### Version conflicts

Use Compose BOM to manage versions automatically:
```kotlin
implementation(platform("androidx.compose:compose-bom:2024.09.00"))
```

### Compiler errors

Ensure all Kotlin artifacts use the same version (2.0.21 in this project).

## References

- [Compose Compiler Plugin Documentation](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compiler.html)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Kotlin 2.0 Release Notes](https://kotlinlang.org/docs/whatsnew20.html)
- [Compose BOM Mapping](https://developer.android.com/jetpack/compose/bom/bom-mapping)
