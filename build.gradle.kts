buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath( "org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    }
}
plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "1.4.32"
}

allprojects {
    repositories {
        google()
        // TODO: Needed for materialdaypicker. Remove ASAP.
        jcenter()
        mavenCentral()
    }
}
