import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    id("kotlin-android")
}
apply(from = "../buildSrc/src/build.gradle")

val baseVersionName = ext.get("baseVersionName") as String

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(29)
        targetSdkVersion(30)
        versionName = baseVersionName
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    lintOptions {
        isAbortOnError = false
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
