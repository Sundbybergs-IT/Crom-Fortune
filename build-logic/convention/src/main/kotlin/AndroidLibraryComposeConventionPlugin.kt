import com.android.build.gradle.LibraryExtension
import com.sundbybergsit.cromfortune.boilerplate.configureAndroidCompose
import com.sundbybergsit.cromfortune.boilerplate.libraries
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            val extension = extensions.getByType<LibraryExtension>()
            configureAndroidCompose(extension)
            dependencies {
                "implementation"(libraries.findBundle("compose").get())
                // Version determined by Compose BoM
                "implementation"("androidx.compose.runtime:runtime-livedata")
                // Version determined by Compose BoM
                "implementation"("androidx.compose.material3:material3")
                // Version determined by Compose BoM
                "implementation"("androidx.compose.ui:ui-tooling")
            }
        }
    }

}
