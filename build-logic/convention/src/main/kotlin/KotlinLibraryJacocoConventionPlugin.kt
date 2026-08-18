import com.sundbybergsit.cromfortune.boilerplate.configureJacoco
import com.sundbybergsit.cromfortune.boilerplate.configureKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinLibraryJacocoConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.gradle.jacoco")
                apply("org.gradle.java-library")
                apply("org.jetbrains.kotlin.jvm")
            }
            configureKotlin()
            configureJacoco()
        }
    }

}
