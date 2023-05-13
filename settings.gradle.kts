rootProject.buildFileName = "build.gradle.kts"

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":app",
    ":algorithm",
    ":domain"
)

project(":domain").projectDir = file("$rootDir/domain")
