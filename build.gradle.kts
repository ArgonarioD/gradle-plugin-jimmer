import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.gradle.plugin-publish") version "1.2.1"
    `embedded-kotlin`
}

group = "tech.argonariod"
version = "1.1.1"

repositories {
    mavenCentral()
}

dependencies {
    @Suppress("PackageUpdate", "RedundantSuppression")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.8.0-1.0.9")
    @Suppress("PackageUpdate", "RedundantSuppression")
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.8.0")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
}

gradlePlugin {
    website = "https://github.com/ArgonarioD/gradle-plugin-jimmer"
    vcsUrl = "https://github.com/ArgonarioD/gradle-plugin-jimmer"
    plugins {
        create("jimmerPlugin") {
            id = "tech.argonariod.gradle-plugin-jimmer"
            displayName = "Gradle Plugin Jimmer"
            description = "A plugin designed to simplify the integration of Java/Kotlin projects with Jimmer."
            tags = listOf("jimmer")
            implementationClass = "tech.argonariod.gradle.jimmer.JimmerPlugin"
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        apiVersion = "1.8"
        languageVersion = "1.8"
        compilerOptions {
            javaParameters = true
            freeCompilerArgs.apply {
                add("-Xjvm-default=all")
                add("-Xsam-conversions=class")
                add("-Xjsr305=strict")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}