rootProject.buildFileName = "build.gradle.kts"
rootProject.name = "crom-fortune"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":app",
    ":algorithm",
    ":domain"
)
