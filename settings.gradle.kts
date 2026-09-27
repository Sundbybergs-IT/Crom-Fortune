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
    ":core:ui",
    ":feature:settings",
    ":algorithm:algorithmApi",
    ":algorithm:algorithmCore",
    ":algorithm:cromFortuneV1",
    ":domain"
)
project(":core:ui").projectDir = file("$rootDir/core/ui")
project(":feature:settings").projectDir = file("$rootDir/feature/settings")
project(":algorithm:algorithmApi").projectDir = file("$rootDir/algorithm/algorithm-api")
project(":algorithm:algorithmCore").projectDir = file("$rootDir/algorithm/algorithm-core")
project(":algorithm:cromFortuneV1").projectDir = file("$rootDir/algorithm/crom-fortune-v1")
