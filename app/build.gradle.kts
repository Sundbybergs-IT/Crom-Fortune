import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.serialization")
}
apply(from = "../buildSrc/src/build.gradle")

val baseVersionName = ext.get("baseVersionName") as String

android {
    compileSdkVersion(30)

    defaultConfig {
        applicationId = "com.sundbybergsit.cromfortune"
        minSdkVersion(29)
        targetSdkVersion(30)
        versionCode = 80
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
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
    api(projects.domain)
    implementation(projects.algorithm)
    // https://youtrack.jetbrains.com/issue/KT-44452
    //noinspection(DifferentStdlibGradleVersion
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidxConstraintlayout)
    implementation(libs.androidxCore)
    implementation(libs.androidxLifecycleExtensions)
    implementation(libs.androidxLifecycleLivedata)
    implementation(libs.androidxLifecycleRuntime)
    implementation(libs.androidxLifecycleViewmodel)
    implementation(libs.androidxNavigationFragment)
    implementation(libs.androidxNavigationUi)
    implementation(libs.androidxWorkRuntime)
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
