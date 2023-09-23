plugins {
    id("cromfortune.android.library")
    id("cromfortune.android.library.jacoco")
}

android {
    namespace = "com.sundbybergsit.cromfortune.algorithm.cromfortunev1"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    lint {
        abortOnError = false
        checkDependencies = true
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.algorithm.algorithmApi)
    implementation(projects.algorithm.algorithmCore)

    implementation(libs.androidxAnnotation)

    implementation(libs.kotlinxCoroutinesCore)

    testImplementation(libs.androidxTestJunit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinxCoroutinesTest)
}
