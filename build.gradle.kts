buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath(libs.pluginAndroidGradle)
        classpath(libs.pluginKotlinGradle)
    }
}

// https://github.com/gradle/gradle/issues/22797
@Suppress(
    "DSL_SCOPE_VIOLATION",
    "UNRESOLVED_REFERENCE_WRONG_RECEIVER",
)
plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.sonarqube)
}

val baseVersionName = "0.8.9"

allprojects {

    val snapshotVersion = isSnapshotVersion()

    group = "com.sundbybergsit.cromfortune"
    version = "$baseVersionName${if (snapshotVersion) "-SNAPSHOT" else ""}"
    description = "Make a fortune - With Crom Fortune!"

    extra.apply {
        set("baseVersionName", baseVersionName)
        set("snapshotVersion", snapshotVersion)
    }

    repositories {
        mavenCentral()
        google()
    }

}

subprojects {

    sonar {

        properties {
            property("sonar.exclusions", "**/BuildConfig.class,**/R.java,**/R\$*.java,src/main/gen/**/*")
            property("sonar.coverage.exclusions", "build.gradle.kts")
            property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
            property("sonar.junit.reportsPaths", "${buildDir}/test-results/")
            property("sonar.androidLint.reportPaths", "${buildDir}/reports/lint-results-debug.xml")
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
