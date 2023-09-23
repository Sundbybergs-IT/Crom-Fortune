plugins {
    id("cromfortune.kotlin.library.jacoco")
    id("com.android.lint")
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    reports {
        junitXml.setDestination(file("$buildDir/test-results/testDebugUnitTest"))
    }
}

tasks.named("jacocoTestReport") {
    dependsOn(tasks.named("test"))
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
        csv.required.set(false)
        html.required.set(false)
    }
}

dependencies {
    implementation(projects.domain)

    implementation(libs.androidxAnnotation)

    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    testImplementation(kotlin("test"))
    testImplementation(libs.junit5Api)
    testRuntimeOnly(libs.junit5Engine)
    testImplementation(libs.junit5Reporting)
}
