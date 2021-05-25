buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0")
    }
}

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.sonarqube")
}

val baseVersionName = "0.2.14"

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

sonarqube {

    properties {
        property("sonar.projectKey", "com.sundbybergsit.cromfortune")
        property("sonar.organization", "sundbybergsit")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "Crom Fortune :: Android")
        property("sonar.sourceEncoding", "UTF-8")
    }

}
