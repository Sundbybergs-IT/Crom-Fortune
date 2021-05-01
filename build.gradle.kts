buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    }
}
plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "1.4.32"
    id("org.sonarqube") version "3.2.0"
}

allprojects {
    repositories {
        google()
        // TODO: Needed for materialdaypicker. Remove ASAP.
        jcenter()
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "jacoco")

    tasks {
        withType<Test> {
            configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                // https://github.com/gradle/gradle/issues/5184
                excludes = listOf("jdk.internal.*")
            }
        }
        val jacocoTestReport by creating(JacocoReport::class) {
            group = "Reporting"
            description = "Generate Jacoco coverage reports"

            reports {
                csv.isEnabled = false
                html.isEnabled = false
                xml.isEnabled = true
                xml.destination = file("$buildDir/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
            }

            val mainSrc = files("${project.projectDir}/src/main")
            sourceDirectories.setFrom(mainSrc)
            val fileFilter = setOf("**/BuildConfig.class", "src/main/gen/**/*", "src/main/assets/**/*")
            val debugTree = fileTree("${buildDir}/intermediates/javac/debug/classes") { setExcludes(fileFilter) }
            val kotlinDebugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") { setExcludes(fileFilter) }

            classDirectories.setFrom(debugTree, kotlinDebugTree)
            executionData.setFrom(
                    fileTree(project.projectDir) {
                        setIncludes(setOf("**/**/*.exec", "**/**/*.ec"))
                    }
            )
        }
    }

    sonarqube {

        properties {
            property("sonar.exclusions", "**/BuildConfig.class,**/R.java,**/R\$*.java,src/main/gen/**/*")
            property("sonar.sources", "src/main,build.gradle.kts")
            property("sonar.tests", "src/test")
            property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
            property("sonar.junit.reportsPath", "$buildDir/test-results/")
            property("sonar.androidLint.reportPaths", "$buildDir/reports/lint-results-debug.xml")
        }

    }

}

sonarqube {

    properties {
        property("sonar.projectKey", "com.sundbybergsit.cromfortune")
        property("sonar.organization", "sundbybergsit")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "Crom Fortune :: Android")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.coverage.exclusions", "build.gradle.kts")
    }

}
