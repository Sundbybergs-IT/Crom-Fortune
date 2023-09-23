plugins {
    id("cromfortune.android.library")
    id("cromfortune.android.library.jacoco")
}

android {
    namespace = "com.sundbybergsit.cromfortune.algorithm.cromfortunev1"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.algorithm.algorithmApi)
    implementation(projects.algorithm.algorithmCore)

    implementation(libs.androidxAnnotation)

    implementation(libs.kotlinxCoroutinesCore)

    testImplementation(libs.robolectric)
}