plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "com.sundbybergsit.cromfortune"
        minSdkVersion(29)
        targetSdkVersion(30)
        versionCode = 71
        versionName = "0.2.5"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    // https://youtrack.jetbrains.com/issue/KT-44452
    //noinspection(DifferentStdlibGradleVersion
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.work.runtime)
    implementation(libs.materialdaypicker)
    implementation(libs.google.material)
    implementation(libs.google.playCore)
    implementation(libs.yahooFinance)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutinesTest)
    testImplementation(libs.robolectric)
}
