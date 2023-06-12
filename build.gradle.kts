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

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.sonarqube)
}

val baseVersionName = "0.6.1"

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
        // TODO: Needed for materialdaypicker. Remove ASAP.
        jcenter()
        mavenCentral()
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
