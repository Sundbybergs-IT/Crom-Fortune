package com.sundbybergsit.cromfortune.boilerplate

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

private val coverageExclusions = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "src/main/gen/**/*",
    "src/main/assets/**/*",
)

internal fun Project.configureJacoco() {
    tasks.withType<Test> {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
        maxParallelForks = (Runtime.getRuntime().availableProcessors() - 2).coerceAtLeast(1)
    }

    val debugTree = fileTree("$buildDir/intermediates/javac/debug/classes") {
        exclude(coverageExclusions)
    }
    val kotlinDebugTree = fileTree("$buildDir/tmp/kotlin-classes/debug") {
        exclude(coverageExclusions)
    }
    val mainSrc = "${projectDir}/src/main"
    val jacocoTestReport by tasks.register<JacocoReport>("jacocoTestReport") {
        dependsOn("testDebugUnitTest")
        group = "Reporting"
        description = "Generate Jacoco coverage reports"
        reports {
            xml.required.set(true)
            html.required.set(false)
            csv.required.set(false)
        }
        sourceDirectories.setFrom(files(mainSrc))
        classDirectories.setFrom(files(debugTree, kotlinDebugTree))
        executionData.setFrom(fileTree(projectDir) { include("**/**/*.exec", "**/**/*.ec") })
    }
}
