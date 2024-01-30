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
        versionCode = 119
        versionName = baseVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    lint {
        abortOnError = false
        checkDependencies = true
    }
}

dependencies {
    api(projects.domain)
    implementation(projects.algorithm.algorithmApi)
    implementation(projects.algorithm.algorithmCore)
    implementation(projects.algorithm.cromFortuneV1)

    // Version determined by Compose BoM
    debugImplementation( "androidx.compose.ui:ui-test-manifest")
    // Version determined by Compose BoM
    implementation("androidx.compose.material3:material3")
    // Version determined by Compose BoM
    implementation("androidx.compose.ui:ui-tooling")
    implementation(libs.bundles.compose)
    implementation(libs.androidxComposeMaterialIconsExtended)

    implementation(libs.androidxWorkRuntime)
    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxSerializationJson)

    implementation(libs.accompanistNavigationMaterial)
    implementation(libs.androidxNavigationFragment)
    implementation(libs.androidxNavigationUi)

    implementation(libs.androidxAppcompat)
    implementation(libs.androidxCore)
    implementation(libs.androidxLifecycleRuntime)
    implementation(libs.androidxLifecycleViewmodel)

    implementation(libs.googlePlayAppUpdate)
    implementation(libs.googlePlayReview)

    implementation(libs.yahooFinance)

    testImplementation(libs.androidxTestJunit)
    testImplementation(libs.androidxWorkTesting)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinxCoroutinesTest)
    testImplementation(libs.robolectric)
}
