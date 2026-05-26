plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
}

if (project.hasProperty("useKsp")) {
    apply(plugin = "com.google.devtools.ksp")
    extensions.configure<com.google.devtools.ksp.gradle.KspExtension> {
        arg("skipPrivatePreviews", "true")
    }
} else {
    apply(plugin = "kotlin-kapt")
    extensions.configure<org.jetbrains.kotlin.gradle.plugin.KaptExtension> {
        correctErrorTypes = true
        arguments {
            arg("skipPrivatePreviews", "true")
        }
    }
}

android {
    namespace = "com.airbnb.android.showkase_browser_testing"
    // Added to avoid this error -
    // Execution failed for task ':showkase-processor-testing:mergeDebugAndroidTestJavaResource'.
    // > A failure occurred while executing com.android.build.gradle.internal.tasks.Workers$ActionFacade
    // > More than one file was found with OS independent path 'META-INF/gradle/incremental.annotation.processors'
    packagingOptions {
        resources.excludes += "META-INF/gradle/incremental.annotation.processors"
        resources.excludes += "META-INF/*.kotlin_module"
        // Added to avoid this error -
        // Execution failed for task ':app:mergeDebugAndroidTestJavaResource'.
        // > A failure occurred while executing com.android.build.gradle.internal.tasks.MergeJavaResWorkAction
        // > 2 files found with path 'META-INF/AL2.0' from inputs:
        resources.excludes += "META-INF/AL2.0"
        resources.excludes += "META-INF/LGPL2.1"
    }

    defaultConfig {
        minSdk = 26
        compileSdk = 36
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app's state is completely cleared between tests.
        testInstrumentationRunnerArguments["clearPackageData"] = "true"

        if (project.hasProperty("useKsp")) {
            buildConfigField("boolean", "IS_RUNNING_KSP", "true")
        } else {
            buildConfigField("boolean", "IS_RUNNING_KSP", "false")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    configurations {
        all {
            // work around this error:
            // Duplicate class org.intellij.lang.annotations.Identifier found in modules annotations-12.0 (com.intellij:annotations:12.0) and annotations-13.0 (org.jetbrains:annotations:13.0)
            exclude(group = "com.intellij", module = "annotations")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Support Libraries
    implementation(libs.androidx.appcompat)
    // Submodule for testing
    implementation(project(":showkase-browser-testing-submodule"))
    implementation(project(":showkase-browser-testing-submodule-2"))

    // Showkase
    implementation(project(":showkase"))
    if (project.hasProperty("useKsp")) {
        add("ksp", project(":showkase-processor"))
    } else {
        add("kapt", project(":showkase-processor"))
    }
    implementation(project(":showkase-processor"))
    implementation(project(":showkase-screenshot-testing"))

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.tooling)
    androidTestImplementation(libs.compose.ui.test.junit4)

    // Material
    implementation(libs.material)
    implementation(libs.material.compose.theme.adapter)

    // Testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
}
