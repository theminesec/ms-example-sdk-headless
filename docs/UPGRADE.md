# Upgrade Guide: Kotlin Gradle Plugin Upgrade Required for the New SDK

The new Headless SDK release (`versionHeadless = 1.3.08`) upgrades its internal Ktor dependency to `ktorVersion = "3.3.1"`, which in turn requires a newer Kotlin compiler. If you are integrating this SDK version, please apply the following changes to your app's Gradle configuration.

> **Note:** If your app already depends on Ktor directly (e.g. for networking), make sure your version is aligned with (or compatible with) the `3.3.1` used by the SDK. Mixing multiple Ktor versions on the same classpath can cause `NoSuchMethodError` or dependency resolution failures.

## 1. Upgrade the Kotlin Gradle Plugin

In the root `build.gradle.kts`:

```kotlin
kotlin("android") version "2.1.0" apply false
kotlin("plugin.serialization") version "2.1.0" apply false
```

(previously `1.9.25` for both)

- **`kotlin("android")`** is the Kotlin compiler itself and must match (or exceed) the version the SDK was built against.
- **`kotlin("plugin.serialization")`** backs Ktor's JSON serialization (`kotlinx-serialization`). Unlike a regular library dependency, this plugin's version must exactly match the Kotlin compiler version — different versions cannot coexist on the same build. **If your app module applies `org.jetbrains.kotlin.plugin.serialization` or depends on `kotlinx-serialization`, it must be upgraded to 2.1.0 as well.** Upgrading only `kotlin("android")` and missing this will cause a build failure due to a version mismatch.

## 2. Add the Compose Compiler Plugin

Starting with Kotlin 2.0, the Jetpack Compose compiler was extracted from AGP into its own Gradle plugin, so it now needs to be declared explicitly:

- Root `build.gradle.kts`: add `kotlin("plugin.compose") version "2.1.0" apply false`
- Every module that uses Compose: apply `id("org.jetbrains.kotlin.plugin.compose")`

## 3. Remove the Old `composeOptions` Block

In each module's `android { }` block, remove any leftover:

```kotlin
composeOptions {
    kotlinCompilerExtensionVersion = "1.5.15"
}
```

The Compose compiler version is now managed by the plugin added in step 2 — keeping both will cause a conflict.

## 4. R8 Full Mode Compatibility (Amex only)

AGP 8+ enables R8 full mode by default, which corrupts the generic `Signature` attribute on classes created via the `new TypeReference<T>(){}` idiom (Jackson), even when the class is fully `-keep`'d. This surfaces as a crash such as `TypeReference constructed without actual type information`, and affects the Amex SoftPOS kernel path specifically.

If your app enables `minifyEnabled = true`, apply the following:

In `gradle.properties`, add:

```
android.enableR8.fullMode=false
```
