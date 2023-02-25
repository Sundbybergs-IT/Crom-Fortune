rootProject.buildFileName = "build.gradle.kts"

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

include(
    ":app",
    ":algorithm",
    ":domain"
)

project(":domain").projectDir = file("$rootDir/domain")
