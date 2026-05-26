import com.vanniktech.maven.publish.AndroidMultiVariantLibrary

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.airbnb.android.showkase"

    defaultConfig {
        minSdk = 21
        compileSdk = 36
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
        )
    }
    buildFeatures {
        compose = true
    }
    // Added to avoid this error -
    // Execution failed for task ':app:mergeDebugAndroidTestJavaResource'.
    // > A failure occurred while executing com.android.build.gradle.internal.tasks.MergeJavaResWorkAction
    // > 2 files found with path 'META-INF/AL2.0' from inputs:
    packagingOptions {
        resources.excludes += "META-INF/AL2.0"
        resources.excludes += "META-INF/LGPL2.1"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Showkase
    api(project(":showkase-annotation"))

    // Support Libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material)
}

mavenPublishing {
    configure(AndroidMultiVariantLibrary(true, true))
}
