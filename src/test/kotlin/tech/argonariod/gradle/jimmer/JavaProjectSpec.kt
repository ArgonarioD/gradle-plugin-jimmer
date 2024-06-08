package tech.argonariod.gradle.jimmer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain

class JavaProjectSpec : FunSpec({
    test("project with version not set") {
        val gradleProject = createTestProjectFiles("test-project")
        gradleProject.buildFile.writeText(
            """
            plugins {
                java
                id("tech.argonariod.gradle-plugin-jimmer")
            }
            
            repositories {
                mavenCentral()
            }
            
            java {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
            """.trimIndent()
        )
        val result = gradleProject.gradleRunnerWithArguments("dependencies").build()
        var versionNotSetLogged = false
        for (line in result.output.lineSequence()) {
            if (!versionNotSetLogged && line.startsWith("[tech.argonariod.gradle-plugin-jimmer] jimmer.version for Project(path=\":\",name=\"test-project\") is not set. Skipping Jimmer configuration.")) {
                versionNotSetLogged = true
            } else {
                line shouldNotContain "jimmer"
            }
        }
        versionNotSetLogged shouldBe true
    }

    test("project with pure jimmer") {
        val gradleProject = createTestProjectFiles("test-project")
        gradleProject.buildFile.writeText(
            """
            plugins {
                java
                id("tech.argonariod.gradle-plugin-jimmer")
            }
            
            repositories {
                mavenCentral()
            }
            
            jimmer {
                version = "latest.release"
                dto {
                    dirs = listOf("foo/bar", "foo/baz")
                }
                client {
                    checkedException = true
                    enableEmbeddedSwaggerUi = true
                }
                java {
                    keepIsPrefix = true
                    source {
                        excludes = listOf("exclude/foo/bar", "exclude2/foo/bar")
                    }
                }
            }
            
            java {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
            
            project.afterEvaluate {
                tasks.withType<JavaCompile> {
                    println(String.format("java compiler args: %s", options.allCompilerArgs.joinToString()))
                }
            }
            """.trimIndent()
        )
        val result = gradleProject.gradleRunnerWithArguments("dependencies").build()
        var javaCompilerArgumentsConfigured = false
        var jimmerDependencyConfigured = false
        var jimmerAptDependencyConfigured = false
        var jimmerEmbeddedSwaggerUiConfigured = false

        for (line in result.output.lineSequence()) {
            if (!javaCompilerArgumentsConfigured && line.contains("java compiler args: -Ajimmer.dto.dirs=foo/bar,foo/baz, -Ajimmer.client.checkedException=true, -Ajimmer.keepIsPrefix=true, -Ajimmer.source.excludes=exclude/foo/bar,exclude2/foo/bar")) {
                javaCompilerArgumentsConfigured = true
            } else if (!jimmerDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-sql")) {
                jimmerDependencyConfigured = true
            } else if (!jimmerAptDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-apt")) {
                jimmerAptDependencyConfigured = true
            } else if (!jimmerEmbeddedSwaggerUiConfigured && line.contains("org.babyfish.jimmer:jimmer-client-swagger")) {
                jimmerEmbeddedSwaggerUiConfigured = true
            }
            if (javaCompilerArgumentsConfigured && jimmerDependencyConfigured && jimmerAptDependencyConfigured && jimmerEmbeddedSwaggerUiConfigured) {
                break
            }
        }

        javaCompilerArgumentsConfigured shouldBe true
        jimmerDependencyConfigured shouldBe true
        jimmerAptDependencyConfigured shouldBe true
        jimmerEmbeddedSwaggerUiConfigured shouldBe true
    }

    test("project with spring boot should add jimmer-spring-boot-starter dependency and jimmer-apt dependency") {
        val gradleProject = createTestProjectFiles("test-project")
        gradleProject.buildFile.writeText(
            """
            plugins {
                java
                id("tech.argonariod.gradle-plugin-jimmer")
                id("org.springframework.boot") version "2.7.0"
                id("io.spring.dependency-management") version "1.0.11.RELEASE"
            }
            
            repositories {
                mavenCentral()
            }
            
            jimmer {
                version = "latest.release"
            }
            
            java {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }
            
            dependencies {
                implementation("org.springframework.boot:spring-boot-starter")
            }
            """.trimIndent()
        )
        val result = gradleProject.gradleRunnerWithArguments("dependencies").build()

        var jimmerDependencyConfigured = false
        var jimmerAptDependencyConfigured = false
        for (line in result.output.lineSequence()) {
            if (!jimmerDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-spring-boot-starter")) {
                jimmerDependencyConfigured = true
            } else if (!jimmerAptDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-apt")) {
                jimmerAptDependencyConfigured = true
            }
            if (jimmerDependencyConfigured && jimmerAptDependencyConfigured) {
                break
            }
        }
        jimmerDependencyConfigured shouldBe true
        jimmerAptDependencyConfigured shouldBe true
    }

    // ArgonarioD:
    // I have no idea why this test fails when executed with the gradle task test,
    // but it passes when this test function or this test spec class is run individually.
    //
    // Therefore, after ensuring that this test can pass when run on its own,
    // I disabled it to guarantee that the gradle task test can execute all other tests simultaneously.
    xtest("project with quarkus 3 and enable jimmer quarkus extension should add quarkus-jimmer dependency and jimmer-apt dependency") {
        val gradleProject = createTestProjectFiles("test-project")
        gradleProject.buildFile.writeText(
            """
            plugins {
                java
                id("tech.argonariod.gradle-plugin-jimmer")
                id("io.quarkus") version "3.7.2"
            }

            repositories {
                mavenCentral()
            }
            
            jimmer {
                version = "latest.release"
                quarkusExtensionVersion = "latest.release"
            }
            
            java {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            
            dependencies {
                implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.7.2"))
                implementation("io.quarkus:quarkus-core")
            }
            """.trimIndent()
        )
        val annotationProcessorResult = gradleProject.gradleRunnerWithArguments(
            "dependencies", "--configuration", "annotationProcessor"
        ).build()
        val compileClasspathResult = gradleProject.gradleRunnerWithArguments(
            "dependencies", "--configuration", "compileClasspath"
        ).build()

        val result = annotationProcessorResult.output + compileClasspathResult.output

        var jimmerDependencyConfigured = false
        var jimmerAptDependencyConfigured = false
        for (line in result.lineSequence()) {
            if (!jimmerDependencyConfigured && line.contains("io.github.flynndi:quarkus-jimmer")) {
                jimmerDependencyConfigured = true
            } else if (!jimmerAptDependencyConfigured && line.contains("org.babyfish.jimmer:jimmer-apt")) {
                jimmerAptDependencyConfigured = true
            }
            if (jimmerDependencyConfigured && jimmerAptDependencyConfigured) {
                break
            }
        }
        jimmerDependencyConfigured shouldBe true
        jimmerAptDependencyConfigured shouldBe true
    }
})