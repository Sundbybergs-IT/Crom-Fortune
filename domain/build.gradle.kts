plugins {
    id("cromfortune.kotlin.library.jacoco")
    id("com.android.lint")
    id("kotlinx-serialization")
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

lint {
    abortOnError = false
    checkDependencies = true
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(libs.androidxAnnotation)
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxSerializationJson)
    testImplementation(libs.junit5Api)
    testRuntimeOnly(libs.junit5Engine)
    testImplementation(libs.junit5Reporting)
}
