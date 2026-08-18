plugins {
    id("cromfortune.kotlin.library.jacoco")
    id("com.android.lint")
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
    implementation(projects.algorithm.algorithmApi)
    implementation(projects.domain)
    implementation(libs.androidxAnnotation)
    implementation(libs.kotlinStdlib)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit5Api)
    testRuntimeOnly(libs.junit5Engine)
    testImplementation(libs.junit5Reporting)
}
