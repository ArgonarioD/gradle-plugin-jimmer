package tech.argonariod.gradle.jimmer.kotlin

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import tech.argonariod.gradle.jimmer.JimmerExtension
import tech.argonariod.gradle.jimmer.MavenArtifactIds
import tech.argonariod.gradle.jimmer.getJimmerVersion
import tech.argonariod.gradle.jimmer.util.*

internal fun Project.configureAsKotlinProject(jimmerExtension: JimmerExtension) {
    val jimmerVersion = getJimmerVersion(jimmerExtension) ?: return
    configureJimmerKotlinDependencies(
        jimmerVersion,
        jimmerExtension.ormCompileOnly.getOrElse(false),
        jimmerExtension.client.enableEmbeddedSwaggerUi.getOrElse(false)
    )
    plugins.withType(KotlinPluginWrapper::class.java) {
        configureKotlinSourceSets()
    }
    plugins.withType(KspGradleSubplugin::class.java) {
        jimmerExtension.applyToKspExtension(extensions.getByType(KspExtension::class.java))
    }

    if (plugins.hasPlugin(MavenArtifactIds.QUARKUS_PLUGIN_ID)) {
        resolveQuarkusKspConflicts()
    }
}

private fun Project.configureJimmerKotlinDependencies(
    jimmerVersion: String,
    ormCompileOnly: Boolean,
    enableEmbeddedSwaggerUi: Boolean
) {
    val dependencyHandler = dependencies
    val implementationDependencies = configurations.implementationConfiguration.dependencies
    val compileOnlyDependencies = configurations.compileOnlyConfiguration.dependencies

    if (implementationDependencies.any { it.name.startsWith("spring-boot-starter") }) {
        implementationDependencies.add(
            dependencyHandler.createJimmerDependency(
                MavenArtifactIds.JIMMER_SPRING_BOOT_STARTER_ARTIFACT_ID,
                jimmerVersion
            )
        )
    } else if (ormCompileOnly) {
        implementationDependencies.add(
            dependencyHandler.createJimmerDependency(
                MavenArtifactIds.JIMMER_CORE_KOTLIN_ARTIFACT_ID,
                jimmerVersion
            )
        )
        compileOnlyDependencies.add(
            dependencyHandler.createJimmerDependency(
                MavenArtifactIds.JIMMER_SQL_KOTLIN_ARTIFACT_ID,
                jimmerVersion
            )
        )
    } else {
        implementationDependencies.add(
            dependencyHandler.createJimmerDependency(
                MavenArtifactIds.JIMMER_SQL_KOTLIN_ARTIFACT_ID,
                jimmerVersion
            )
        )
    }

    configurations.kspConfiguration.dependencies.add(
        dependencyHandler.createJimmerDependency(
            MavenArtifactIds.JIMMER_KSP_ARTIFACT_ID,
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

private fun Project.configureKotlinSourceSets() {
    val sourceSets = extensions.getByType(KotlinJvmProjectExtension::class.java).sourceSets
    sourceSets.getByName("main").kotlin.srcDir("build/generated/ksp/main/kotlin")
}

private fun JimmerExtension.applyToKspExtension(kspExtension: KspExtension) =
    with(kspExtension) {
        dto.apply {
            mutable.letNotNull {
                arg("jimmer.dto.mutable", it.toString())
            }
            dirs.letNotEmpty {
                arg("jimmer.dto.dirs", it.joinToArgument())
            }
            testDirs.letNotEmpty {
                arg("jimmer.dto.testDirs", it.joinToArgument())
            }
        }
        client.apply {
            checkedException.letNotNull {
                arg("jimmer.client.checkedException", it.toString())
            }
        }
    }

private fun Project.resolveQuarkusKspConflicts() {
    getTasksByName("quarkusGenerateCode", true).forEach { task ->
        task.setDependsOn(
            task.dependsOn.filterIsInstance<Provider<Task>>().filter { it.get().name != "processResources" })
    }
    getTasksByName("quarkusGenerateCodeDev", true).forEach { task ->
        task.setDependsOn(
            task.dependsOn.filterIsInstance<Provider<Task>>().filter { it.get().name != "processResources" })
    }
}