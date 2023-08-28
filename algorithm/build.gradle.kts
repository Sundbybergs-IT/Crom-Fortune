plugins {
    id("com.android.library")
    id("kotlin-android")
}
apply(from = "../buildSrc/src/build.gradle")

val baseVersionName = ext.get("baseVersionName") as String

android {
    namespace = "com.sundbybergsit.cromfortune.algorithm"
    compileSdk = 34
    defaultConfig {
        minSdk = 29
        targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(projects.domain)
    implementation(libs.androidxAppcompat)
    implementation(project(mapOf("path" to ":domain")))
    testImplementation(libs.junit)
}
