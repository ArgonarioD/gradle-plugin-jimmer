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

```groovy
// build.gradle
import tech.argonariod.gradle.jimmer.JimmerLanguage
import tech.argonariod.gradle.jimmer.JimmerDtoNullableInputModifier

jimmer {
    version = 'latest.release'
    disableVersionNotSetWarning = false
    quarkusExtensionVersion = 'latest.release'
    language = JimmerLanguage.JAVA
    ormCompileOnly = false
    dto {
        dirs = ['src/main/dto', 'src/main/dto2']
        testDirs = ['src/test/dto', 'src/test/dto2']
        defaultNullableInputModifier = JimmerDtoNullableInputModifier.STATIC
        hibernateValidatorEnhancement = false
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
        entry {
            objects = 'Objects'
            tables = 'Tables'
            tableExes = 'TableExes'
            fetchers = 'Fetchers'
        }
    }
}
```

```kotlin
// build.gradle.kts
import tech.argonariod.gradle.jimmer.JimmerLanguage
import tech.argonariod.gradle.jimmer.JimmerDtoNullableInputModifier

jimmer {
    version = "latest.release"
    disableVersionNotSetWarning = false
    quarkusExtensionVersion = "latest.release"
    language = JimmerLanguage.KOTLIN
    ormCompileOnly = false
    immutable {
        isModuleRequired = false
    }
    dto {
        mutable = true
        dirs = listOf("src/main/dto", "src/main/dto2")
        testDirs = listOf("src/test/dto", "src/test/dto2")
        defaultNullableInputModifier = JimmerDtoNullableInputModifier.STATIC
        hibernateValidatorEnhancement = false
    }
    client {
        enableEmbeddedSwaggerUi = true
        checkedException = true
    }
}
```

| Argument Name                      | Type                             | Default Value            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|------------------------------------|----------------------------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `version`                          | `String`                         | None                     | The version of Jimmer to be used. <br/>This property follows the rules defined in the ["Declaring Versions"](https://docs.gradle.org/current/userguide/single_versions.html) section of the Gradle documentation. Therefore, when this property is set to `"latest.release"` or `"+"`, your project will be configured to depend on the latest version of Jimmer from your configured repository.                                                                                                                                                                                                                   |
| `disableVersionNotSetWarning`      | `Boolean`                        | `false`                  | Whether to disable the warning that is displayed when the `version` property is not set.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `quarkusExtensionVersion`          | `String`                         | None                     | The version of the **UNOFFICIAL** Quarkus extension `io.github.flynndi:quarkus-jimmer` to be used. For more detailed information, please refer to [its GitHub repository page](https://github.com/flynndi/quarkus-jimmer-extension). <br/>This property follows the rules defined in the ["Declaring Versions"](https://docs.gradle.org/current/userguide/single_versions.html) section of the Gradle documentation. Therefore, when this property is set to `"latest.release"` or `"+"`, your project will be configured to depend on the latest version of the Quarkus extension from your configured repository. |
| `language`                         | `JimmerLanguage`                 | Configured Automatically | The programming language of the project. It will be configured automatically by plugin, but you could also set it manually if you need.                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| `ormCompileOnly`                   | `Boolean`                        | `false`                  | Whether to add the Jimmer ORM dependencies as `compileOnly` dependencies.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `immutable`                        |                                  |                          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| \> `isModuleRequired`              | `Boolean`                        | `false`                  | Whether to generate `JimmerModule` class. <br/>This argument is only available for Kotlin, and the `JimmerModule` class is only needed for compatibility with version 0.7.47 and earlier.                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `dto`                              |                                  |                          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| \> `mutable`                       | `Boolean`                        | `false`                  | Whether to generate DTO properties as immutable `var`.<br/>This argument is only available for Kotlin.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| \> `dirs`                          | `List<String>`                   | `["src/main/dto"]`       | The directories where the jimmer-dto files are located.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| \> `testDirs`                      | `List<String>`                   | `["src/test/dto"]`       | The directories where the jimmer-dto files are located in the test source set.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| \> `defaultNullableInputModifier`  | `JimmerDtoNullableInputModifier` | `STATIC`                 | Default handling mode of nullable input properties.<br/>See details in [Handle Null Values](https://babyfish-ct.github.io/jimmer-doc/docs/mutation/save-command/input-dto/null-handling/#higher-level-configurations).                                                                                                                                                                                                                                                                                                                                                                                              |
| \> `hibernateValidatorEnhancement` | `Boolean`                        | `false`                  | Input DTO with Dynamic fields will implement `org.hibernate.validator.Incubating.HibernateValidatorEnhancedBean` interface.<br/>See details in [babyfish-ct/jimmer#561](https://github.com/babyfish-ct/jimmer/issues/561).                                                                                                                                                                                                                                                                                                                                                                                          |
| `client`                           |                                  |                          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| \> `enableEmbeddedSwaggerUi`       | `Boolean`                        | `false`                  | Use embedded swagger-ui js and css files instead of referencing from unpkg cdn.<br/>Enable this option will cause the packaged application to be larger, but it can be used when you have trouble with accessing unpkg cdn.                                                                                                                                                                                                                                                                                                                                                                                         |
| \> `checkedException`              | `Boolean`                        | `false`                  | Whether to generate client exceptions as checked exceptions.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| \> `ignoreJdkWarning`              | `Boolean`                        | `false`                  | Whether to suppress the error that Java compiler version which lower than 11 will lead to generating incorrect client code.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `java`                             |                                  |                          | Java only configurations.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| \> `keepIsPrefix`                  | `Boolean`                        | `false`                  | Whether to keep the `is` prefix of `boolean` type properties of immutable entities.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| \> `source`                        |                                  |                          |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| \> \> `includes`                   | `List<String>`                   | `[]`                     | The package names and class names that will be processed by the precompiler. If not empty, the precompiler will only process the classes matching this arg.                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| \> \> `excludes`                   | `List<String>`                   | `[]`                     | The package names and class names that will not be processed by the precompiler.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| \> `entry`                         |                                  |                          | Class name configuration of composite jimmer entities.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| \> \> `objects`                    | `String`                         | `Objects`                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| \> \> `tables`                     | `String`                         | `Tables`                 |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| \> \> `tableExes`                  | `String`                         | `TableExes`              |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| \> \> `fetchers`                   | `String`                         | `Fetchers`               |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |


