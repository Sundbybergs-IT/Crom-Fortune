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
    ":algorithm:cromFortuneV1",
    ":domain"
)
project(":algorithm:algorithmApi").projectDir = file("$rootDir/algorithm/algorithm-api")
project(":algorithm:algorithmCore").projectDir = file("$rootDir/algorithm/algorithm-core")
project(":algorithm:cromFortuneV1").projectDir = file("$rootDir/algorithm/crom-fortune-v1")
