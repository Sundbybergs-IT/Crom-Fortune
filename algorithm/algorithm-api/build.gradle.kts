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
    implementation(projects.domain)
    implementation(libs.androidxAnnotation)
    implementation(libs.kotlinStdlib)

    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit5Engine)
    testImplementation(libs.junit5Api)
    testImplementation(libs.junit5Reporting)

}
