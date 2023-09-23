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
    ":algorithm:algorithmApi",
    ":algorithm:algorithmCore",
    ":domain"
)
project(":algorithm:algorithmApi").projectDir = file("$rootDir/algorithm/algorithm-api")
project(":algorithm:algorithmCore").projectDir = file("$rootDir/algorithm/algorithm-core")
