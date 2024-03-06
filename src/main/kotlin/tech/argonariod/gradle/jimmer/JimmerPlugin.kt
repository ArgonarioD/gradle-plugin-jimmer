package tech.argonariod.gradle.jimmer

import org.gradle.api.Plugin
import org.gradle.api.Project
import tech.argonariod.gradle.jimmer.java.configureAsJavaProject
import tech.argonariod.gradle.jimmer.kotlin.configureAsKotlinProject

@Suppress("unused")
class JimmerPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        val extension = extensions.create("jimmer", JimmerExtension::class.java)

        afterEvaluate {
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