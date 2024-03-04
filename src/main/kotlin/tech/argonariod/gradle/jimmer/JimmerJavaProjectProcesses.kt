package tech.argonariod.gradle.jimmer

import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import tech.argonariod.gradle.jimmer.util.*

internal fun Project.configureAsJavaProject(jimmerExtension: JimmerExtension) {
    val jimmerVersion = getJimmerVersion(jimmerExtension) ?: return
    configureJimmerJavaDependencies(
        jimmerVersion,
        jimmerExtension.quarkusExtensionVersion.orNull,
        jimmerExtension.client.enableEmbeddedSwaggerUi.getOrElse(false)
    )

    tasks.withType(JavaCompile::class.java).configureEach {
        jimmerExtension.applyAnnotationProcessorArguments(it)
    }
}

private fun Project.configureJimmerJavaDependencies(
    jimmerVersion: String,
    quarkusExtensionVersion: String?,
    enableEmbeddedSwaggerUi: Boolean
) {
    val dependencyHandler = dependencies
    with(configurations.implementationConfiguration.dependencies) {
        if (quarkusExtensionVersion != null) {
            add(
                dependencyHandler.create(
                    MavenArtifactIds.QUARKUS_JIMMER_GROUP_ID,
                    MavenArtifactIds.QUARKUS_JIMMER_ARTIFACT_ID,
                    quarkusExtensionVersion
                )
            )
        } else if (any { it.name.startsWith("spring-boot-starter") }) {
            add(
                dependencyHandler.createJimmerDependency(
                    MavenArtifactIds.JIMMER_SPRING_BOOT_STARTER_ARTIFACT_ID,
                    jimmerVersion
                )
            )
        } else {
            add(
                dependencyHandler.createJimmerDependency(
                    MavenArtifactIds.JIMMER_SQL_JAVA_ARTIFACT_ID,
                    jimmerVersion
                )
            )
        }
    }

    configurations.aptConfiguration.dependencies.add(
        dependencyHandler.createJimmerDependency(
            MavenArtifactIds.JIMMER_APT_ARTIFACT_ID,
            jimmerVersion
        )
    )

    if (enableEmbeddedSwaggerUi) {
        configurations.runtimeOnlyConfiguration.dependencies.add(
            dependencyHandler.createJimmerDependency(
                MavenArtifactIds.JIMMER_CLIENT_SWAGGER_ARTIFACT_ID,
                jimmerVersion
            )
        )
    }
}

private fun JimmerExtension.applyAnnotationProcessorArguments(javaCompileTask: JavaCompile) {
    javaCompileTask.options.compilerArgs.also { args ->
        dto.apply {
            dirs.letNotEmpty {
                args.add("-Ajimmer.dto.dirs=${it.joinToArgument()}")
            }
            testDirs.letNotEmpty {
                args.add("-Ajimmer.dto.testDirs=${it.joinToArgument()}")
            }
        }
        client.apply {
            checkedException.letNotNull {
                args.add("-Ajimmer.client.checkedException=${it}")
            }
            ignoreJdkWarning.letNotNull {
                args.add("-Ajimmer.client.ignoreJdkWarning=${it}")
            }
        }
        java.apply {
            keepIsPrefix.letNotNull {
                args.add("-Ajimmer.keepIsPrefix=${it}")
            }
            source.apply {
                includes.letNotEmpty {
                    args.add("-Ajimmer.source.includes=${it.joinToArgument()}")
                }
                excludes.letNotEmpty {
                    args.add("-Ajimmer.source.excludes=${it.joinToArgument()}")
                }
            }
        }
    }
}