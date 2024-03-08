package tech.argonariod.gradle.jimmer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.FileSystems

class KotlinProjectSpec : FunSpec({
    val kotlinVersion = "1.9.0"

    test("project with version not set") {
        val gradleProject = createTestProjectFiles("test-project")
        gradleProject.buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "${kotlinVersion}"
                id("tech.argonariod.gradle-plugin-jimmer")
            }
            
            repositories {
                mavenCentral()
            }
            """.trimIndent()
        )
        val result = gradleProject.gradleRunnerWithArguments("dependencies").build()
        var versionNotSetLogged = false
        for (line in result.output.lineSequence()) {
            if (!versionNotSetLogged && line.startsWith("[tech.argonariod.gradle-plugin-jimmer] jimmer.version is not set. Skipping Jimmer configuration.")) {
                versionNotSetLogged = true
            } else {
                line shouldNotContain "jimmer"
            }
        }
        versionNotSetLogged shouldBe true
    }

    test("project with no ksp") {
        val gradleProject = createTestProjectFiles("test-project")
        gradleProject.buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "${kotlinVersion}"
                id("tech.argonariod.gradle-plugin-jimmer")
            }
            
            repositories {
                mavenCentral()
            }
            
            jimmer {
                version = "latest.release"
            }
            """.trimIndent()
        )
        val result = gradleProject.gradleRunnerWithArguments("dependencies").build()
        var jimmerDependencyConfigured = false
        for (line in result.output.lineSequence()) {
            if (!jimmerDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-sql-kotlin")) {
                jimmerDependencyConfigured = true
            }
            if (jimmerDependencyConfigured) {
                break
            }
        }
        jimmerDependencyConfigured shouldBe true
    }

    test("project with pure jimmer") {
        val gradleProject = createTestProjectFiles("test-project")
        gradleProject.buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "${kotlinVersion}"
                id("tech.argonariod.gradle-plugin-jimmer")
                id("com.google.devtools.ksp") version "${kotlinVersion}+"
            }
            
            repositories {
                mavenCentral()
            }
            
            jimmer {
                version = "latest.release"
                dto {
                    mutable = true
                    dirs = listOf("foo/bar", "foo/baz")
                }
                client {
                    enableEmbeddedSwaggerUi = true
                }
            }
            
            project.afterEvaluate {
                with (kotlin.sourceSets) {
                    println(getByName("main").kotlin.srcDirs.joinToString())
                }
                println(ksp.arguments.entries.joinToString { (k, v) -> String.format("%s: %s", k, v) })
            }
            """.trimIndent()
        )
        val result = gradleProject.gradleRunnerWithArguments("dependencies").build()

        val separator = FileSystems.getDefault().separator.let {
            if (it == "\\") "\\\\" else it
        }

        val expectedKotlinSourceSetsRegex =
            Regex(".*src${separator}main${separator}kotlin.*build${separator}generated${separator}ksp${separator}main${separator}kotlin.*")
        var kotlinSourceSetsConfigured = false
        var kspArgumentsConfigured = false
        var jimmerDependencyConfigured = false
        var jimmerKspDependencyConfigured = false
        var jimmerEmbeddedSwaggerUiConfigured = false
        for (line in result.output.lineSequence()) {
            if (!kotlinSourceSetsConfigured && line.contains(expectedKotlinSourceSetsRegex)) {
                kotlinSourceSetsConfigured = true
            } else if (!kspArgumentsConfigured && line == "jimmer.dto.mutable: true, jimmer.dto.dirs: foo/bar,foo/baz") {
                kspArgumentsConfigured = true
            } else if (!jimmerDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-sql-kotlin")) {
                jimmerDependencyConfigured = true
            } else if (!jimmerKspDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-ksp")) {
                jimmerKspDependencyConfigured = true
            } else if (!jimmerEmbeddedSwaggerUiConfigured && line.contains("org.babyfish.jimmer:jimmer-client-swagger")) {
                jimmerEmbeddedSwaggerUiConfigured = true
            }
            if (kotlinSourceSetsConfigured && kspArgumentsConfigured && jimmerDependencyConfigured && jimmerKspDependencyConfigured && jimmerEmbeddedSwaggerUiConfigured) {
                break
            }
        }
        kotlinSourceSetsConfigured shouldBe true
        kspArgumentsConfigured shouldBe true
        jimmerDependencyConfigured shouldBe true
        jimmerKspDependencyConfigured shouldBe true
        jimmerEmbeddedSwaggerUiConfigured shouldBe true
    }

    test("project with spring boot should add jimmer-spring-boot-starter dependency and jimmer-ksp dependency") {
        val gradleProject = createTestProjectFiles("test-project")
        gradleProject.buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "${kotlinVersion}"
	            kotlin("plugin.spring") version "${kotlinVersion}"
                id("org.springframework.boot") version "2.7.0"
                id("tech.argonariod.gradle-plugin-jimmer")
                id("com.google.devtools.ksp") version "${kotlinVersion}+"
            }
            
            repositories {
                mavenCentral()
            }
            
            jimmer {
                version = "latest.release"
            }
            
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter")
            }
            """.trimIndent()
        )
        val result = gradleProject.gradleRunnerWithArguments("dependencies").build()

        var jimmerDependencyConfigured = false
        var jimmerKspDependencyConfigured = false
        for (line in result.output.lineSequence()) {
            if (!jimmerDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-spring-boot-starter")) {
                jimmerDependencyConfigured = true
            } else if (!jimmerKspDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-ksp")) {
                jimmerKspDependencyConfigured = true
            }
            if (jimmerDependencyConfigured && jimmerKspDependencyConfigured) {
                break
            }
        }
        jimmerDependencyConfigured shouldBe true
        jimmerKspDependencyConfigured shouldBe true
    }

    test("project with mapstruct") {
        val gradleProject = createTestProjectFiles("test-project")
        gradleProject.buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "${kotlinVersion}"
                id("tech.argonariod.gradle-plugin-jimmer")
                id("com.google.devtools.ksp") version "${kotlinVersion}+"
                kotlin("kapt") version "${kotlinVersion}"
            }
            
            repositories {
                mavenCentral()
            }
            
            jimmer {
                version = "latest.release"
            }
            
            dependencies {
                implementation("org.mapstruct:mapstruct:1.5.3.Final")
                kapt("org.mapstruct:mapstruct-processor:1.5.3.Final")
            }
            """.trimIndent()
        )
        val result = gradleProject.gradleRunnerWithArguments("dependencies").build()

        var jimmerDependencyConfigured = false
        var jimmerKspDependencyConfigured = false
        var jimmerMapstructKaptDependencyConfigured = false

        for (line in result.output.lineSequence()) {
            if (!jimmerDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-sql-kotlin")) {
                jimmerDependencyConfigured = true
            } else if (!jimmerKspDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-ksp")) {
                jimmerKspDependencyConfigured = true
            } else if (!jimmerMapstructKaptDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-mapstruct-apt")) {
                jimmerMapstructKaptDependencyConfigured = true
            }
            if (jimmerDependencyConfigured && jimmerKspDependencyConfigured && jimmerMapstructKaptDependencyConfigured) {
                break
            }
        }
        jimmerDependencyConfigured shouldBe true
        jimmerKspDependencyConfigured shouldBe true
        jimmerMapstructKaptDependencyConfigured shouldBe true
    }

    // ArgonarioD:
    // I have no idea why this test fails when executed with the gradle task test,
    // but it passes when this test function or this test spec class is run individually.
    //
    // Therefore, after ensuring that this test can pass when run on its own,
    // I disabled it to guarantee that the gradle task test can execute all other tests simultaneously.
    xtest("project with quarkus should resolve gradle task dependencies") {
        val gradleProject = createTestProjectFiles("test-project")
        gradleProject.buildFile.writeText(
            """
            plugins {
                kotlin("jvm") version "${kotlinVersion}"
                kotlin("plugin.allopen") version "${kotlinVersion}"
                id("tech.argonariod.gradle-plugin-jimmer")
                id("com.google.devtools.ksp") version "${kotlinVersion}+"
                id("io.quarkus") version "3.7.2"
            }
            
            repositories {
                mavenCentral()
            }
            
            jimmer {
                version = "latest.release"
            }
            
            dependencies {
                implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.7.2"))
                implementation("io.quarkus:quarkus-core")
            }
            
            project.afterEvaluate {
                getTasksByName("quarkusGenerateCode", true).forEach { task ->
                    println("quarkusGenerateCode exists")
                    task.dependsOn.filterIsInstance<Provider<Task>>().forEach {
                        println(String.format("quarkusGenerateCodeDependency: ", it.get().name))
                    }
                }
                getTasksByName("quarkusGenerateCodeDev", true).forEach { task ->
                    println("quarkusGenerateCodeDev exists")
                    task.dependsOn.filterIsInstance<Provider<Task>>().forEach {
                        println(String.format("quarkusGenerateCodeDevDependency: ", it.get().name))
                    }
                }
            }
            """.trimIndent()
        )
        val result = gradleProject.gradleRunnerWithArguments("dependencies").build()
        var quarkusGenerateCodeDependencyConfigured = false
        val qgcTaskDependencyRegex = Regex("quarkusGenerateCodeDependency: .*processResources.*")
        var quarkusGenerateCodeDevDependencyConfigured = false
        val qgcdTaskDependencyRegex = Regex("quarkusGenerateCodeDevDependency: .*processResources.*")
        var jimmerDependencyConfigured = false
        var jimmerKspDependencyConfigured = false
        for (line in result.output.lineSequence()) {
            if (!quarkusGenerateCodeDependencyConfigured) {
                line shouldNotContain qgcTaskDependencyRegex
            }
            if (!quarkusGenerateCodeDevDependencyConfigured) {
                line shouldNotContain qgcdTaskDependencyRegex
            }
            if ((!quarkusGenerateCodeDependencyConfigured || !quarkusGenerateCodeDevDependencyConfigured) && line.startsWith(
                    "> Task :dependencies"
                )
            ) {
                quarkusGenerateCodeDevDependencyConfigured = true
                quarkusGenerateCodeDependencyConfigured = true
            }

            if (!jimmerDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-sql-kotlin")) {
                jimmerDependencyConfigured = true
            } else if (!jimmerKspDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-ksp")) {
                jimmerKspDependencyConfigured = true
            }
            if (quarkusGenerateCodeDependencyConfigured && quarkusGenerateCodeDevDependencyConfigured && jimmerDependencyConfigured && jimmerKspDependencyConfigured) {
                break
            }
        }
        quarkusGenerateCodeDependencyConfigured shouldBe true
        quarkusGenerateCodeDevDependencyConfigured shouldBe true
        jimmerDependencyConfigured shouldBe true
        jimmerKspDependencyConfigured shouldBe true
    }
})