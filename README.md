<h1 align="center">Gradle Plugin Jimmer</h1>

<p align="center">A plugin designed to simplify the integration of Java/Kotlin projects with Jimmer.</p>

<div align="center">
<a href="LICENSE"> 
    <img src="https://img.shields.io/github/license/ArgonarioD/gradle-plugin-jimmer" alt="License">
</a>
</div>

<p align="center">
English | <a href="README_zh_CN.md">简体中文</a>
</p>

## Features

- Automatically detects the project language and, after configuring the version string, adds the corresponding language's dependencies for that version. It also supports manually defining the project language:
    - Java: `jimmer-sql` and `jimmer-apt`
    - Kotlin: `jimmer-sql-kotlin` and `jimmer-ksp`
- If the project is a Spring Boot project, use `jimmer-spring-boot-starter` instead of `jimmer-sql` and `jimmer-sql-kotlin`.
- If the project depends on MapStruct with Kotlin and KAPT, add the `jimmer-mapstruct-apt` kapt dependency automatically.
- Supports one-configuration addition and version management of dependencies for embedding Jimmer in Swagger UI.
- Supports one-configuration addition and version management of the **unofficial** Quarkus extension dependency `io.github.flynndi:quarkus-jimmer`.
- If the project is a Quarkus Kotlin project, it automatically resolves the circular dependency between Quarkus and the ksp code generation tasks that prevents your build.
- Strongly typed modification of Jimmer code generator parameters, instead of manually passing string parameters to APT or KSP.

## Usage

Add the following content to your project's build file:

```groovy
// build.gradle
plugins {
    id 'tech.argonariod.gradle-plugin-jimmer' version 'latest.release'
}

jimmer {
    version = 'latest.release'
}
```

```kotlin
// build.gradle.kts
plugins {
    id("tech.argonariod.gradle-plugin-jimmer") version "latest.release"
}

jimmer {
    version = "latest.release"
}
```

### Java Projects

Reload your Gradle project, and the latest versions of `org.babyfish.jimmer:jimmer-sql` and `org.babyfish.jimmer:jimmer-apt` will be added as dependencies to your project.

### Kotlin Projects

1. Ensure that the `com.google.devtools.ksp` plugin is added to your project, for example:
    ```kotlin
    plugins {
        kotlin("jvm") version "1.8.0"
        id("com.google.devtools.ksp") version "1.8.0+"
    }
    ```
2. Reload your Gradle project, and the latest versions of `org.babyfish.jimmer:jimmer-sql-kotlin` and `org.babyfish.jimmer:jimmer-ksp` will be added as dependencies to your project.

> [!NOTE]
> If your project does not include the `com.google.devtools.ksp` plugin, this plugin will only add dependencies such as `org.babyfish.jimmer:jimmer-sql-kotlin` to your project, without adding the `org.babyfish.jimmer:jimmer-ksp` dependency.

### Spring Boot Projects

If any dependencies in your project have an artifact ID starting with `spring-boot-starter`, then `org.babyfish.jimmer:jimmer-spring-boot-starter` will replace `jimmer-sql` and `jimmer-sql-kotlin` mentioned above as dependencies in your project.

## All Configurable Items

This section only lists all configurable items; please refer to their documentation comments for detailed information.

```groovy
// build.gradle
import tech.argonariod.gradle.jimmer.JimmerLanguage

jimmer {
    version = 'latest.release'
    disableVersionNotSetWarning = false
    quarkusExtensionVersion = 'latest.release'
    language = JimmerLanguage.JAVA
    ormCompileOnly = false
    dto {
        dirs = ['src/main/dto', 'src/main/dto2']
        testDirs = ['src/test/dto', 'src/test/dto2']
    }
    client {
        enableEmbeddedSwaggerUi = true
        checkedException = true
        ignoreJdkWarning = true
    }
    java {
        keepIsPrefix = false
        source {
            includes = ['foo.bar', 'foo.bar.Test1']
            excludes = ['foo.bar.Test2']
        }
    }
}
```

```kotlin
// build.gradle.kts
import tech.argonariod.gradle.jimmer.JimmerLanguage

jimmer {
    version = "latest.release"
    disableVersionNotSetWarning = false
    language = JimmerLanguage.KOTLIN
    ormCompileOnly = false
    dto {
        mutable = true
        dirs = listOf("src/main/dto", "src/main/dto2")
        testDirs = listOf("src/test/dto", "src/test/dto2")
    }
    client {
        enableEmbeddedSwaggerUi = true
        checkedException = true
    }
}
```