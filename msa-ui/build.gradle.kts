plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.theminesec.example.headless"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.theminesec.example.headless.compose.msaui"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"

            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation(platform("androidx.compose:compose-bom:2025.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation(project(":landing"))

    // SDK channel selection, for CI to build against either the internal RC
    // (QA verification, before promote) or the formal/released SDK (default,
    // also what a plain local build uses):
    //   ./gradlew :msa-ui:assembleDebug                                        -> release channel, versionHeadless from gradle.properties
    //   ./gradlew :msa-ui:assembleDebug -PsdkChannel=rc -PsdkVersion=1.3.24-rc.2 -> RC from ms-registry-internal
    //   ./gradlew :msa-ui:assembleDebug -PsdkVersion=1.3.25                     -> a specific formal release
    val versionHeadless: String by project
    val sdkChannel = (project.findProperty("sdkChannel") as String?) ?: "release"
    val sdkVersion = (project.findProperty("sdkVersion") as String?) ?: versionHeadless
    val sdkArtifactId = if (sdkChannel == "rc") "headless-stage-rc" else "headless-stage"
    implementation("com.theminesec.sdk:$sdkArtifactId:$sdkVersion")
    //releaseImplementation("com.theminesec.sdk:headless:$versionHeadless")
}
