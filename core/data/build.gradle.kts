plugins {
    id("cromfortune.android.library")
    id("cromfortune.android.library.jacoco")
    id("kotlinx-serialization")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.sundbybergsit.cromfortune.core.data"
}

dependencies {
    api(projects.domain)

    implementation(libs.androidxAnnotation)
    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.roomRuntime)
    implementation(libs.roomKtx)
    implementation(libs.androidxDatastorePreferences)

    ksp(libs.roomCompiler)

    testImplementation(libs.androidxTestJunit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinxCoroutinesTest)
    testImplementation(libs.robolectric)
    testImplementation(libs.roomTesting)
}
