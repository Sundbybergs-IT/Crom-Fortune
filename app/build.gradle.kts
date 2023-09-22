import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.serialization")
}
apply(from = "../buildSrc/src/build.gradle")

val baseVersionName = ext.get("baseVersionName") as String

android {
    namespace = "com.sundbybergsit.cromfortune"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.sundbybergsit.cromfortune"
        minSdk = 29
        targetSdk = 34
        versionCode = 111
        versionName = baseVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("int", "MIN_SDK_VERSION", "$minSdk")
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }
    lint {
        abortOnError = false
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { test ->
            test.testLogging {
                events.addAll(listOf(PASSED, SKIPPED, FAILED))
                showCauses = true
                showExceptions = true
                exceptionFormat = FULL
            }
        }
    }
}

dependencies {
    api(projects.domain)
    implementation(platform(libs.composeBom))
    // Version determined by Compose BoM
    debugImplementation( "androidx.compose.ui:ui-test-manifest")
    // Version determined by Compose BoM
    implementation("androidx.compose.material3:material3")
    // Version determined by Compose BoM
    implementation("androidx.compose.ui:ui-tooling")
    implementation(projects.algorithm)
    // https://youtrack.jetbrains.com/issue/KT-44452
    //noinspection(DifferentStdlibGradleVersion
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.accompanistNavigationMaterial)
    implementation(libs.androidxAppcompat)
    implementation(libs.androidxCore)
    implementation(libs.androidxLifecycleRuntime)
    implementation(libs.androidxLifecycleViewmodel)
    implementation(libs.androidxNavigationFragment)
    implementation(libs.androidxNavigationUi)
    implementation(libs.androidxWorkRuntime)
    implementation(libs.bundles.compose)
    implementation(libs.googlePlayAppUpdate)
    implementation(libs.googlePlayReview)
    implementation(libs.yahooFinance)
    testImplementation(libs.androidxTestJunit)
    testImplementation(libs.androidxWorkTesting)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinxCoroutinesTest)
    testImplementation(libs.robolectric)
}
