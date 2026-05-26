plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
}

if (project.hasProperty("useKsp")) {
    apply(plugin = "com.google.devtools.ksp")
} else {
    apply(plugin = "kotlin-kapt")
    extensions.configure<org.jetbrains.kotlin.gradle.plugin.KaptExtension> {
        correctErrorTypes = true
        arguments {
            arg("multiPreviewType", "com.airbnb.android.submodule.showkasesample.FontPreview")
        }
    }
}

android {
    namespace = "com.airbnb.android.submodule.showkasesample"

    defaultConfig {
        minSdk = 21
        compileSdk = 36
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
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
    // Support Libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Showkase
    implementation(project(":showkase"))
    implementation(project(":sample-submodule"))

    if (project.hasProperty("useKsp")) {
        add("ksp", project(":showkase-processor"))
    } else {
        add("kapt", project(":showkase-processor"))
    }

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.constraintlayout.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.material)
    implementation(libs.compose.runtime.saveable)
    implementation(libs.compose.runtime.livedata)
    androidTestImplementation(libs.compose.ui.test.junit4)
}
