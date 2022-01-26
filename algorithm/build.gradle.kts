import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("kotlin-android")
}
apply(from = "../buildSrc/src/build.gradle")

val baseVersionName = ext.get("baseVersionName") as String

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 29
        targetSdk = 31
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = "11"

        // For creation of default methods in interfaces
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

dependencies {
    implementation(projects.domain)
    implementation(libs.androidxAppcompat)
    implementation(project(mapOf("path" to ":domain")))
    testImplementation(libs.junit)
}
