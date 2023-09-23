import com.android.build.gradle.LibraryExtension
import com.sundbybergsit.cromfortune.boilerplate.configureKotlinAndroid
import com.sundbybergsit.cromfortune.boilerplate.libraries
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }
            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libraries.findVersion("compileSdk").get().toString().toInt()
            }
            dependencies {
                // Version determined by Kotlin plugin
                add("testImplementation", kotlin("test"))
            }
        }
    }

}
