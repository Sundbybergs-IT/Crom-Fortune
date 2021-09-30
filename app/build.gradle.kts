plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.serialization")
}
apply(from = "../buildSrc/src/build.gradle")

val baseVersionName = ext.get("baseVersionName") as String

android {
    namespace = "com.sundbybergsit.cromfortune"
    compileSdk = 31
    defaultConfig {
        applicationId = "com.sundbybergsit.cromfortune"
        minSdk = 29
        targetSdk = 31
        versionCode = 105
        versionName = baseVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-alpha02"
    }
    lint {
        isAbortOnError = false
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    api(projects.domain)
    implementation(projects.algorithm)
    // https://youtrack.jetbrains.com/issue/KT-44452
    //noinspection(DifferentStdlibGradleVersion
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidxConstraintlayoutConstraintlayout)
    implementation(libs.androidxCore)
    implementation(libs.androidxLifecycleExtensions)
    implementation(libs.androidxLifecycleLivedata)
    implementation(libs.androidxLifecycleRuntime)
    implementation(libs.androidxLifecycleViewmodel)
    implementation(libs.androidxNavigationFragment)
    implementation(libs.androidxNavigationUi)
    implementation(libs.androidxWorkRuntime)
    implementation(libs.bundles.compose)
    implementation(libs.materialdaypicker)
    implementation(libs.googleMaterial)
    implementation(libs.googlePlayCore)
    implementation(libs.yahooFinance)
    testImplementation(libs.androidxTestJunit)
    testImplementation(libs.androidxWorkTesting)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinxCoroutinesTest)
    testImplementation(libs.robolectric)
}
