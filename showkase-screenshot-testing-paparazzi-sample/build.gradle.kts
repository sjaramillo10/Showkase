plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.paparazzi)
    alias(libs.plugins.compose.compiler)
}

if (project.hasProperty("useKsp")) {
    apply(plugin = "com.google.devtools.ksp")
} else {
    apply(plugin = "kotlin-kapt")
    extensions.configure<org.jetbrains.kotlin.gradle.plugin.KaptExtension> {
        correctErrorTypes = true
    }
}

android {
    namespace = "com.airbnb.android.showkase.screenshot.testing.paparazzi"
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
        minSdk = 21
        compileSdk = 36
        targetSdk = 33
        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app's state is completely cleared between tests.
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    buildFeatures {
        compose = true
    }
}

// https://github.com/cashapp/paparazzi/issues/409
tasks.withType<Test>().configureEach {
    jvmArgs(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
    )
}

kotlin {
    jvmToolchain(17)
}

// NOTE: The original Groovy build had an afterEvaluate block that manually
// registered build/generated/ksp/<variant>/kotlin as a source folder per
// variant (workaround for https://github.com/google/ksp/issues/37, fixed
// in KSP 1.0.7 / June 2022). KSP 2.x — the version we use — registers
// generated Kotlin sources automatically, so the block was a no-op and
// has been dropped during the KTS conversion. If KSP source registration
// regresses, restore via the LibraryExtension API.

dependencies {
    // Showkase
    implementation(project(":showkase"))
    if (project.hasProperty("useKsp")) {
        add("ksp", project(":showkase-processor"))
        add("kspAndroidTest", project(":showkase-processor"))
        add("kspTest", project(":showkase-processor"))
    } else {
        add("kapt", project(":showkase-processor"))
        add("kaptAndroidTest", project(":showkase-processor"))
        add("kaptTest", project(":showkase-processor"))
    }
    api(project(":showkase-screenshot-testing"))

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

    // Image loading
    implementation(libs.picasso)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)
    implementation(libs.test.parameter.injector)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(project(":showkase-screenshot-testing-paparazzi"))
}
