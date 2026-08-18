plugins {
    id("cromfortune.kotlin.library.jacoco")
    id("com.android.lint")
    id("kotlinx-serialization")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    reports {
        junitXml.outputLocation.set(layout.buildDirectory.dir("test-results/testDebugUnitTest"))
    }
}

lint {
    abortOnError = false
    checkDependencies = true
}

dependencies {
    implementation(libs.androidxAnnotation)
    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.kotlinStdlib)

    testRuntimeOnly(libs.junit5Engine)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit5Api)
    testImplementation(libs.junit5Reporting)
}
