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
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "src/main/gen/**/*",
    "src/main/assets/**/*",
    "**/BR.class",
    "**/DataBinderMapperImpl.class",
    "**/DataBindingInfo.class",
    "**/databinding/*Binding.class",
)

internal fun Project.configureJacoco() {
    pluginManager.withPlugin("com.android.application") {
        extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
            buildTypes.configureEach {
                if (name == "debug") {
                    enableUnitTestCoverage = true
                }
            }
        }
    }
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<com.android.build.api.dsl.LibraryExtension> {
            buildTypes.configureEach {
                if (name == "debug") {
                    enableUnitTestCoverage = true
                }
            }
        }
    }

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
            xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
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
            val kotlinDebugTree2 = fileTree(layout.buildDirectory.dir("intermediates/kotlin-classes/debug")) {
                exclude(coverageExclusions)
            }
            sourceDirectories.setFrom(files("${projectDir}/src/main/java", "${projectDir}/src/main/kotlin"))
            classDirectories.setFrom(files(debugTree, kotlinDebugTree, kotlinDebugTree2))
        } else {
            dependsOn("test")
            val mainTree = fileTree(layout.buildDirectory.dir("classes/kotlin/main")) {
                exclude(coverageExclusions)
            }
            sourceDirectories.setFrom(files("${projectDir}/src/main/java", "${projectDir}/src/main/kotlin"))
            classDirectories.setFrom(files(mainTree))
        }

        executionData.setFrom(fileTree(layout.buildDirectory) {
            include(
                "**/*.exec",
                "**/*.ec"
            )
        })
    }
}
