buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath(libs.pluginAndroidGradle)
        classpath(libs.pluginFirebaseCrashlyticsGradle)
        classpath(libs.pluginGoogleServices)
        classpath(libs.pluginKotlinGradle)
    }
}

plugins {
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.serialization)
    alias(libs.plugins.sonarqube)
}

val baseVersionName = "0.9.3"

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
            val buildDir = layout.buildDirectory.get().asFile
            property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
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
        property("sonar.coverage.exclusions", "**/build.gradle.kts,**/R.class,**/R\$*.class,**/BuildConfig.*,**/Manifest*.*,**/BR.class,**/DataBinderMapperImpl.class,**/DataBindingInfo.class,**/databinding/*Binding.class")
        property("sonar.qualitygate.wait", "true")
    }

}
