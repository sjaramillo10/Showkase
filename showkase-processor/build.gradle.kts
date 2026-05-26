import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("com.vanniktech.maven.publish")
}

kotlin {
    jvmToolchain(17)
}

// Need to apply to all tasks like this so test tasks also get these compiler args
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlin.contracts.ExperimentalContracts",
            "-Xopt-in=androidx.room.compiler.processing.ExperimentalProcessingApi",
            "-Xopt-in=com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview",
        )
    }
}

dependencies {
    implementation(project(":showkase-annotation"))

    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.javapoet)
    implementation(libs.kotlin.metadata.jvm)
    implementation(libs.ksp.api)
    implementation(libs.room.compiler.processing)

    testImplementation(libs.strikt.core)
    testImplementation(libs.junit)
    testImplementation(libs.room.compiler.processing.testing)
}

mavenPublishing {
    configure(JavaLibrary(JavadocJar.Javadoc(), true))
}
