package com.sundbybergsit.cromfortune.boilerplate

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidComposeApp(
    applicationExtension: ApplicationExtension,
) {
    applicationExtension.apply {
        buildFeatures {
            compose = true
        }

        dependencies {
            val bom = libraries.findLibrary("composeBom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
        }
    }

    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
}

internal fun Project.configureAndroidComposeLibrary(
    libraryExtension: LibraryExtension,
) {
    libraryExtension.apply {
        buildFeatures {
            compose = true
        }

        dependencies {
            val bom = libraries.findLibrary("composeBom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
        }
    }

    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
}
