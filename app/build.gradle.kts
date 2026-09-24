import java.util.Properties

plugins {
    id("cromfortune.android.application")
    id("cromfortune.android.application.compose")
    id("cromfortune.android.application.firebase")
    id("cromfortune.android.application.jacoco")
    id("kotlinx-serialization")
    alias(libs.plugins.compose.compiler)
}

val baseVersionName = ext.get("baseVersionName") as String
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}
val logoDevPublishableKey = providers.gradleProperty("LOGO_DEV_PUBLISHABLE_KEY")
    .orElse(providers.environmentVariable("LOGO_DEV_PUBLISHABLE_KEY"))
    .orElse(providers.provider { localProperties.getProperty("LOGO_DEV_PUBLISHABLE_KEY", "") })
    .getOrElse("")
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "com.sundbybergsit.cromfortune.main"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.sundbybergsit.cromfortune"
        versionCode = 155
        versionName = baseVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "LOGO_DEV_PUBLISHABLE_KEY", "\"$logoDevPublishableKey\"")
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

    platform(libs.composeBom)
    debugImplementation( libs.uiTestManifest)
    implementation(libs.material3)
    implementation(libs.uiTooling)
    implementation(libs.bundles.compose)
    implementation(libs.androidxComposeMaterialIconsExtended)
    implementation(libs.coilCompose)
    implementation(libs.coilNetworkOkhttp)

    implementation(libs.androidxWorkRuntime)
    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxSerializationJson)

    implementation(libs.accompanistNavigationMaterial)
    implementation(libs.androidxNavigationFragment)
    implementation(libs.androidxNavigationUi)
    implementation(libs.androidxNavigation3Runtime)
    implementation(libs.androidxNavigation3Ui)
    implementation(libs.androidxLifecycleViewmodelNavigation3)

    implementation(libs.androidxAppcompat)
    implementation(libs.androidxCore)
    implementation(libs.androidxLifecycleRuntime)
    implementation(libs.androidxLifecycleViewmodel)

    implementation(libs.googlePlayAppUpdate)
    implementation(libs.googlePlayAppUpdateKtx)
    implementation(libs.googlePlayReview)

    implementation(libs.yahooFinance)

    testImplementation(libs.androidxComposeUiTestJunit)
    testImplementation(libs.androidxTestJunit)
    testImplementation(libs.androidxWorkTesting)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinxCoroutinesTest)
    testImplementation(libs.robolectric)
}
