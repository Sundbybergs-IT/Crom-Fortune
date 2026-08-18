package com.sundbybergsit.cromfortune.boilerplate

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
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

    if (tasks.findByName("jacocoTestReport") == null) {
        tasks.register<JacocoReport>("jacocoTestReport")
    }

    tasks.named<JacocoReport>("jacocoTestReport").configure {
        group = "Reporting"
        description = "Generate Jacoco coverage reports"
        reports {
            xml.required.set(true)
            html.required.set(false)
            csv.required.set(false)
        }

        val isAndroid = plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")

        if (isAndroid) {
            dependsOn("testDebugUnitTest")
            val debugTree = fileTree(layout.buildDirectory.dir("intermediates/javac/debug/classes")) {
                exclude(coverageExclusions)
            }
            val kotlinDebugTree = fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                exclude(coverageExclusions)
            }
            val mainSrc = "${projectDir}/src/main/kotlin"
            sourceDirectories.setFrom(files(mainSrc))
            classDirectories.setFrom(files(debugTree, kotlinDebugTree))
        } else {
            dependsOn("test")
            val mainTree = fileTree(layout.buildDirectory.dir("classes/kotlin/main")) {
                exclude(coverageExclusions)
            }
            val mainSrc = "${projectDir}/src/main/kotlin"
            sourceDirectories.setFrom(files(mainSrc))
            classDirectories.setFrom(files(mainTree))
        }

        executionData.setFrom(fileTree(projectDir) {
            include(
                "**/**/*.exec",
                "**/**/*.ec",
                "**/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "**/jacoco/*.exec"
            )
        })
    }
}
