buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.pluginAndroidGradle)
        classpath(libs.pluginKotlinGradle)
    }
}

@Suppress(
    "DSL_SCOPE_VIOLATION",
    "UNRESOLVED_REFERENCE_WRONG_RECEIVER",
)
plugins {
    kotlin("plugin.serialization") version libs.versions.kotlin
    alias(libs.plugins.sonarqube)
}

val baseVersionName = "0.7.0"

allprojects {

    val snapshotVersion = isSnapshotVersion()

    extra.apply {
        set("baseVersionName", baseVersionName)
        set("snapshotVersion", snapshotVersion)
    }

    group = "com.sundbybergsit.cromfortune"
    version = "$baseVersionName${if (snapshotVersion) "-SNAPSHOT" else ""}"
    description = "Make a fortune - With Crom Fortune!"

    repositories {
        google()
        mavenCentral()
    }

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                // For creation of default methods in interfaces
                "-Xjvm-default=all",
                // Avoid having to stutter experimental annotations all over the codebase
                "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi",
                "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-Xopt-in=androidx.compose.runtime.ExperimentalComposeApi",
                "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi",
                "-Xopt-in=com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi",
                "-Xopt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi",
                "-Xopt-in=kotlin.ExperimentalUnsignedTypes",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=kotlinx.coroutines.InternalCoroutinesApi"
            )
        }
    }

}

fun isSnapshotVersion(): Boolean {
    val envSnapshotVersion = System.getenv("snapshotVersion")
    return envSnapshotVersion?.toBoolean() ?: true
}

sonar {

    properties {
        property("sonar.projectKey", "Sundbybergs-IT_Crom-Fortune")
        property("sonar.organization", "sundbybergs-it")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "Crom Fortune :: Android")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.coverage.exclusions", "**/build.gradle.kts,")
        property("sonar.qualitygate.wait", "true")
    }

}
