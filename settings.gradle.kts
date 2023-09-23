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
    ":algorithmApi",
    ":algorithmCore",
    ":domain"
)
project(":algorithmApi").projectDir = file("$rootDir/algorithm-api")
project(":algorithmCore").projectDir = file("$rootDir/algorithm-core")
