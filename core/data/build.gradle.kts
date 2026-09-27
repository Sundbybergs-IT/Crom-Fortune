plugins {
    id("cromfortune.android.library")
    id("cromfortune.android.library.jacoco")
    id("kotlinx-serialization")
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

    testImplementation(libs.androidxTestJunit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinxCoroutinesTest)
    testImplementation(libs.robolectric)
}
