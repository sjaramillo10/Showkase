plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("shot")
    alias(libs.plugins.compose.compiler)
}

if (project.hasProperty("useKsp")) {
    apply(plugin = "com.google.devtools.ksp")
    kotlin {
        sourceSets.configureEach {
            kotlin.srcDir("build/generated/ksp/$name/kotlin")
        }
    }
} else {
    apply(plugin = "kotlin-kapt")
    extensions.configure<org.jetbrains.kotlin.gradle.plugin.KaptExtension> {
        correctErrorTypes = true
    }
}

android {
    namespace = "com.airbnb.android.showkasesample"

    defaultConfig {
        applicationId = "com.airbnb.android.showkasesample"
        minSdk = 21
        compileSdk = 36
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.karumi.shot.ShotTestRunner"
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
        resources.excludes += "META-INF/gradle/incremental.annotation.processors"
    }
}

if (project.hasProperty("useKsp")) {
    extensions.configure<com.google.devtools.ksp.gradle.KspExtension> {
        arg("skipPrivatePreviews", "true")
    }
} else {
    extensions.configure<org.jetbrains.kotlin.gradle.plugin.KaptExtension> {
        arguments {
            arg("skipPrivatePreviews", "true")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Showkase dependencies
    // TODO(vinaygaba): Using debugImplementation was causing kapt related NonExistentClass errors.
    //  Figure out a way to enable using debugImplementation
    implementation(project(":showkase"))
    implementation(project(":sample-submodule"))
    implementation(project(":sample-submodule-2"))
    if (project.hasProperty("useKsp")) {
        add("ksp", project(":showkase-processor"))
        add("kspAndroidTest", project(":showkase-processor"))
    } else {
        add("kapt", project(":showkase-processor"))
        add("kaptAndroidTest", project(":showkase-processor"))
    }

    // Support Libraries
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

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

    // Image loading
    implementation(libs.picasso)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(project(":showkase-screenshot-testing-shot"))
}
