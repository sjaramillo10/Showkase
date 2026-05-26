import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.android.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.shot.plugin)
        classpath(libs.maven.publish.plugin)
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.compose.compiler)
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    repositories {
        google()
        mavenCentral()
    }
    extensions.configure<DetektExtension> {
        config.setFrom(rootProject.files("detekt/detekt.yml"))
    }
    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
    }
}
