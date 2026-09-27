plugins {
    id("cromfortune.android.library")
    id("cromfortune.android.library.compose")
    id("cromfortune.android.library.jacoco")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.sundbybergsit.cromfortune.feature.settings"
}

dependencies {
    implementation(projects.core.ui)
    implementation(libs.androidxLifecycleViewmodel)

    debugImplementation(libs.uiTestManifest)
    testImplementation(libs.androidxComposeUiTestJunit)
    testImplementation(libs.androidxTestJunit)
    testImplementation(libs.robolectric)
}
