package tech.argonariod.gradle.jimmer.java

import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import tech.argonariod.gradle.jimmer.JimmerExtension
import tech.argonariod.gradle.jimmer.MavenArtifactIds
import tech.argonariod.gradle.jimmer.getJimmerVersion
import tech.argonariod.gradle.jimmer.util.*

internal fun Project.configureAsJavaProject(jimmerExtension: JimmerExtension) {
    val jimmerVersion = getJimmerVersion(jimmerExtension) ?: return
    configureJimmerJavaDependencies(
        jimmerVersion,
        jimmerExtension.quarkusExtensionVersion.orNull,
        jimmerExtension.ormCompileOnly.getOrElse(false),
        jimmerExtension.client.enableEmbeddedSwaggerUi.getOrElse(false)
    )

    tasks.withType(JavaCompile::class.java).configureEach {
        jimmerExtension.applyAnnotationProcessorArguments(it)
    }
}

private fun Project.configureJimmerJavaDependencies(
    jimmerVersion: String,
    quarkusExtensionVersion: String?,
    ormCompileOnly: Boolean,
    enableEmbeddedSwaggerUi: Boolean
) {
    val dependencyHandler = dependencies
    val implementationDependencies = configurations.implementationConfiguration.dependencies
    val compileOnlyDependencies = configurations.compileOnlyConfiguration.dependencies

    if (quarkusExtensionVersion != null) {
        implementationDependencies.add(
            dependencyHandler.create(
                MavenArtifactIds.QUARKUS_JIMMER_GROUP_ID,
                MavenArtifactIds.QUARKUS_JIMMER_ARTIFACT_ID,
                quarkusExtensionVersion
            )
        )
    } else if (implementationDependencies.any { it.name.startsWith("spring-boot-starter") }) {
        implementationDependencies.add(
            dependencyHandler.createJimmerDependency(
                MavenArtifactIds.JIMMER_SPRING_BOOT_STARTER_ARTIFACT_ID,
                jimmerVersion
            )
        )
    } else if (ormCompileOnly) {
        implementationDependencies.add(
            dependencyHandler.createJimmerDependency(
                MavenArtifactIds.JIMMER_CORE_JAVA_ARTIFACT_ID,
                jimmerVersion
            )
        )
        compileOnlyDependencies.add(
            dependencyHandler.createJimmerDependency(
                MavenArtifactIds.JIMMER_SQL_JAVA_ARTIFACT_ID,
                jimmerVersion
            )
        )
    } else {
        implementationDependencies.add(
            dependencyHandler.createJimmerDependency(
                MavenArtifactIds.JIMMER_SQL_JAVA_ARTIFACT_ID,
                jimmerVersion
            )
        )
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
            defaultNullableInputModifier.letNotNull {
                args.add("-Ajimmer.dto.defaultNullableInputModifier=${it.value}")
            }
            hibernateValidatorEnhancement.letNotNull {
                args.add("-Ajimmer.dto.hibernateValidatorEnhancement=${it}")
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
            entry.apply {
                objects.letNotNull {
                    args.add("-Ajimmer.entry.objects=${it}")
                }
                tables.letNotNull {
                    args.add("-Ajimmer.entry.tables=${it}")
                }
                tableExes.letNotNull {
                    args.add("-Ajimmer.entry.tableExes=${it}")
                }
                fetchers.letNotNull {
                    args.add("-Ajimmer.entry.fetchers=${it}")
                }
            }
        }
    }
}