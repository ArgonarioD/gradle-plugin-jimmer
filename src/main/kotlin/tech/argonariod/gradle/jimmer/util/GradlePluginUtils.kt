package tech.argonariod.gradle.jimmer.util

import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import tech.argonariod.gradle.jimmer.MavenArtifactIds

internal fun Project.findClasspathArtifactModuleVersion(moduleName: String): String? {
    return this.buildscript
        .configurations
        .getByName("classpath")
        .resolvedConfiguration
        .resolvedArtifacts
        .map { it.moduleVersion.id }
        .firstOrNull { it.name == moduleName }
        ?.version
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun DependencyHandler.create(groupId: String, artifactId: String, version: String): Dependency =
    create("$groupId:$artifactId:$version")

@Suppress("NOTHING_TO_INLINE")
internal inline fun DependencyHandler.createJimmerDependency(artifactId: String, version: String): Dependency =
    create(MavenArtifactIds.JIMMER_GROUP_ID, artifactId, version)

internal inline val ConfigurationContainer.implementationConfiguration
    get() = getByName("implementation")

internal inline val ConfigurationContainer.runtimeOnlyConfiguration
    get() = getByName("runtimeOnly")

internal inline val ConfigurationContainer.kspConfiguration
    get() = getByName("ksp")

internal inline val ConfigurationContainer.aptConfiguration
    get() = getByName("annotationProcessor")

