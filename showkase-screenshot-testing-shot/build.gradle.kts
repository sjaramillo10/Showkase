import com.vanniktech.maven.publish.AndroidMultiVariantLibrary

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("shot")
    alias(libs.plugins.compose.compiler)
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.airbnb.android.showkase.screenshot.testing.shot"
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
        testApplicationId = "com.airbnb.android.showkase.screenshot.testing.shot"
        minSdk = 21
        compileSdk = 36
        targetSdk = 33
        testInstrumentationRunner = "com.karumi.shot.ShotTestRunner"
        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app's state is completely cleared between tests.
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }
    buildFeatures {
        compose = true
    }
}

shot {
    applicationId = "com.airbnb.android.showkase.screenshot.testing.shot"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Showkase
    api(project(":showkase"))
    api(libs.compose.ui.test.junit4)
    api(project(":showkase-screenshot-testing"))
    api(libs.shot.android)

    // Testing
    api(libs.androidx.test.core)
    api(libs.androidx.test.rules)
    api(libs.androidx.test.runner)
}

mavenPublishing {
    configure(AndroidMultiVariantLibrary(true, true))
}
