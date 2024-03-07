package tech.argonariod.gradle.jimmer

import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencySet
import tech.argonariod.gradle.jimmer.java.configureAsJavaProject
import tech.argonariod.gradle.jimmer.kotlin.configureAsKotlinProject
import tech.argonariod.gradle.jimmer.util.createJimmerDependency
import tech.argonariod.gradle.jimmer.util.kspConfiguration

@Suppress("unused")
class JimmerPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create("jimmer", JimmerExtension::class.java)

        // If the project does not contain any KSP configurations in the project dependency configuration during evaluation,
        // the KSP plugin will not create a KSP task.
        //
        // Therefore, provide a fake dependency first, and then remove that dependency after evaluation.
        lateinit var kspDependencies: DependencySet
        lateinit var tempPlaceholderKspDependency: Dependency

        plugins.withType(KspGradleSubplugin::class.java) {
            kspDependencies = configurations.kspConfiguration.dependencies
            tempPlaceholderKspDependency = dependencies.createJimmerDependency(
                MavenArtifactIds.JIMMER_KSP_ARTIFACT_ID,
                "latest.release"
            )
            kspDependencies.add(tempPlaceholderKspDependency)
        }

        afterEvaluate {
            kspDependencies.remove(tempPlaceholderKspDependency)
            when (extension.language.orNull) {
                JimmerLanguage.JAVA -> configureAsJavaProject(extension)
                JimmerLanguage.KOTLIN -> configureAsKotlinProject(extension)
                JimmerLanguage.BOTH -> {
                    configureAsJavaProject(extension)
                    configureAsKotlinProject(extension)
                }

                null -> {
                    if (plugins.hasPlugin(MavenArtifactIds.KOTLIN_PLUGIN_ID)) {
                        configureAsKotlinProject(extension)
                    } else {
                        configureAsJavaProject(extension)
                    }
                }
            }
        }
    }
}