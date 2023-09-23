plugins {
    id("cromfortune.android.application")
    id("cromfortune.android.application.compose")
    id("cromfortune.android.application.jacoco")
    id("kotlinx-serialization")
}

val baseVersionName = ext.get("baseVersionName") as String

android {
    namespace = "com.sundbybergsit.cromfortune.main"
    defaultConfig {
        applicationId = "com.sundbybergsit.cromfortune"
        versionCode = 111
        versionName = baseVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    api(projects.domain)
    // Version determined by Compose BoM
    debugImplementation( "androidx.compose.ui:ui-test-manifest")
    // Version determined by Compose BoM
    implementation("androidx.compose.material3:material3")
    // Version determined by Compose BoM
    implementation("androidx.compose.ui:ui-tooling")
    implementation(projects.algorithm)
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
