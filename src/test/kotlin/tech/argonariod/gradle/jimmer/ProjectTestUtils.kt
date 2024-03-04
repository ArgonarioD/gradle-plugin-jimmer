package tech.argonariod.gradle.jimmer

import io.kotest.core.spec.Spec
import io.kotest.engine.spec.tempdir
import org.gradle.testkit.runner.GradleRunner
import java.io.File

data class TestGradleProject(
    val gradleProjectDir: File,
    val settingsFile: File,
    val buildFile: File
) {
    fun gradleRunnerWithArguments(vararg arguments: String) = GradleRunner.create()
        .withProjectDir(gradleProjectDir)
        .withPluginClasspath()
        .forwardOutput()
        .withArguments(*arrayOf("--stacktrace", *arguments))
}

fun Spec.createTestProjectFiles(projectName: String): TestGradleProject {
    val gradleProjectDir = tempdir()
    val settingsFile = File(gradleProjectDir, "settings.gradle.kts")
    val buildFile = File(gradleProjectDir, "build.gradle.kts")
    settingsFile.writeText(
        """
        rootProject.name = "${projectName}"
        """.trimIndent()
    )
    return TestGradleProject(gradleProjectDir, settingsFile, buildFile)
}