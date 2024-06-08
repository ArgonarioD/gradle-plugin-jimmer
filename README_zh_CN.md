<h1 align="center">Gradle Plugin Jimmer</h1>

<p align="center">一个简化 Jimmer 与 Java/Kotlin 项目集成的 Gradle 插件。</p>

<div align="center">
<a href="LICENSE"> 
    <img src="https://img.shields.io/github/license/ArgonarioD/gradle-plugin-jimmer" alt="License">
</a>
</div>

<p align="center">
<a href="README.md">English</a> | 简体中文
</p>

## 功能

- 自动检测项目语言，配置版本号后，添加对应版本的对应语言的依赖项，同时也支持手动定义项目语言：
    - Java: `jimmer-sql` 和 `jimmer-apt`
    - Kotlin: `jimmer-sql-kotlin` 和 `jimmer-ksp`
- 如果项目是 Spring Boot 项目，使用 `jimmer-spring-boot-starter` 代替 `jimmer-sql` 和 `jimmer-sql-kotlin`。
- 如果项目使用了 MapStruct、Kotlin 与 KAPT，自动添加 `jimmer-mapstruct-apt` KAPT 依赖。
- 支持 Jimmer 嵌入 Swagger UI 的依赖的一键添加和版本管理。
- 支持**非官方** Quarkus 扩展 `io.github.flynndi:quarkus-jimmer` 依赖的一键添加和版本管理。
- 如果项目是 Quarkus Kotlin 项目，自动解决 quarkus 与 ksp 代码生成任务的循环依赖导致无法通过编译的问题。
- 强类型修改 Jimmer 代码生成器的参数，而不是手动向 APT 或 KSP 传递字符串参数。

## 使用

在项目的构建文件中添加如下内容：

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

### Java 项目

重新加载你的 Gradle 项目，最新版本的 `org.babyfish.jimmer:jimmer-sql` 和 `org.babyfish.jimmer:jimmer-apt`
就作为依赖项被添加到你的项目中了。

### Kotlin 项目

1. 确保你的项目中添加了 `com.google.devtools.ksp` 插件，如：
    ```kotlin
    plugins {
        kotlin("jvm") version "1.8.0"
        id("com.google.devtools.ksp") version "1.8.0+"
    }
    ```
2. 重新加载你的 Gradle 项目，最新版本的 `org.babyfish.jimmer:jimmer-sql-kotlin` 和 `org.babyfish.jimmer:jimmer-ksp`
   就作为依赖项被添加到你的项目中了。

> [!NOTE]
> 如果你的项目中没有 `com.google.devtools.ksp` 插件，则本插件只会为你的项目添加 `org.babyfish.jimmer:jimmer-sql-kotlin` 等依赖，不会添加 `org.babyfish.jimmer:jimmer-ksp` 依赖。

### Spring Boot 项目

如果你的项目中存在任何工件 ID 的开头为 `spring-boot-starter`
的依赖项，那么 `org.babyfish.jimmer:jimmer-spring-boot-starter` 会代替上文中的 `jimmer-sql` 和 `jimmer-sql-kotlin`
作为依赖项被添加到你的项目中。

## 所有可配置项

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

[//]: # (将README.md中最后的表格翻译成中文。)

| 参数名                                | 类型                               | 默认值                | 描述                                                                                                                                                                                                                                                                                                                                            |
|------------------------------------|----------------------------------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `version`                          | `String`                         | 无                  | Jimmer 依赖项的版本号。<br/>本参数遵循 Gradle 文档中 ["Declaring Versions"](https://docs.gradle.org/current/userguide/single_versions.html) 一节中定义的规则。所以，当本参数的值被设置为 `"latest.release"` or `"+"` 时，你项目中的 Jimmer 版本将被设置为你配置的远程仓库中的最新版本。                                                                                                                          |
| `disableVersionNotSetWarning`      | `Boolean`                        | `false`            | 是否禁用当 `version` 参数没有被设置时的警告。                                                                                                                                                                                                                                                                                                                  |
| `quarkusExtensionVersion`          | `String`                         | 无                  | **非官方**Quarkus 扩展 `io.github.flynndi:quarkus-jimmer` 的版本号。更多信息详见[其 GitHub 仓库](https://github.com/flynndi/quarkus-jimmer-extension)。<br/>本参数遵循 Gradle 文档中 ["Declaring Versions"](https://docs.gradle.org/current/userguide/single_versions.html) 一节中定义的规则。所以，当本参数的值被设置为 `"latest.release"` or `"+"` 时，你项目中的该 Quarkus 扩展的版本将被设置为你配置的远程仓库中的最新版本。 |
| `language`                         | `JimmerLanguage`                 | 自动配置               | 项目所使用的编程语言。本插件会自动配置该参数，但是如果有需要的话你也可以手动设置该参数的值。                                                                                                                                                                                                                                                                                                |
| `ormCompileOnly`                   | `Boolean`                        | `false`            | 是否将 Jimmer 的 ORM 依赖项（`jimmer-sql` 与 `jimmer-sql-kt`）设置为 `compileOnly` 而不是 `implementation`。                                                                                                                                                                                                                                                   |
| `immutable`                        |                                  |                    |                                                                                                                                                                                                                                                                                                                                               |
| \> `isModuleRequired`              | `Boolean`                        | `false`            | 是否生成 `JimmerModule` 类。<br/>本参数仅在 Kotlin 中生效，`JimmerModule` 类仅当你需要适配 0.7.47 或更早的 Jimmer 版本时需要。                                                                                                                                                                                                                                                 |
| `dto`                              |                                  |                    |                                                                                                                                                                                                                                                                                                                                               |
| \> `mutable`                       | `Boolean`                        | `false`            | 是否将 DTO 的属性生成为可变的 `var`。<br/>本参数仅在 Kotlin 中生效。                                                                                                                                                                                                                                                                                                |
| \> `dirs`                          | `List<String>`                   | `['src/main/dto']` | jimmer-dto 文件的源代码目录。                                                                                                                                                                                                                                                                                                                          |
| \> `testDirs`                      | `List<String>`                   | `['src/test/dto']` | jimmer-dto 文件的测试代码目录。                                                                                                                                                                                                                                                                                                                         |
| \> `defaultNullableInputModifier`  | `JimmerDtoNullableInputModifier` | `STATIC`           | DTO 可空属性的默认处理模式修饰符。<br/>更多信息详见[处理空值](https://babyfish-ct.github.io/jimmer-doc/zh/docs/mutation/save-command/input-dto/null-handling/#input-dto%E4%B8%AD%E5%8F%AF%E7%A9%BA%E5%B1%9E%E6%80%A7%E7%9A%844%E7%A7%8D%E5%A4%84%E7%90%86%E6%96%B9%E6%B3%95)。                                                                                          |
| \> `hibernateValidatorEnhancement` | `Boolean`                        | `false`            | 是否令具有动态属性的 Input DTO 实现 `org.hibernate.validator.Incubating.HibernateValidatorEnhancedBean` 接口。<br/>更多信息详见[babyfish-ct/jimmer#561](https://github.com/babyfish-ct/jimmer/issues/561)。                                                                                                                                                         |
| `client`                           |                                  |                    |                                                                                                                                                                                                                                                                                                                                               |
| \> `enableEmbeddedSwaggerUi`       | `Boolean`                        | `false`            | 是否在应用中嵌入 Swagger UI 的 js 与 css 文件而不是直接从 unpkg cdn 引用。<br/>启用本选项会令你的应用包占用更多空间，但当你访问 unpkg cdn 不稳定时比较有用。                                                                                                                                                                                                                                        |
| \> `checkedException`              | `Boolean`                        | `false`            | 客户端异常是否生成受检异常。                                                                                                                                                                                                                                                                                                                                |
| \> `ignoreJdkWarning`              | `Boolean`                        | `false`            | 是否抑制当编译器版本低于 11 时导致生成错误的客户端代码时抛出的错误。                                                                                                                                                                                                                                                                                                          |
| `java`                             |                                  |                    | 仅 Java 配置。                                                                                                                                                                                                                                                                                                                                    |
| \> `keepIsPrefix`                  | `Boolean`                        | `false`            | 是否保留实体 `boolean` 类型的 `is` 前缀。                                                                                                                                                                                                                                                                                                                 |
| \> `source`                        |                                  |                    |                                                                                                                                                                                                                                                                                                                                               |
| \> \> `includes`                   | `List<String>`                   | `[]`               | 预编译器所处理的包名或类名。如果该参数非空，预编译器只处理能够匹配该参数的类。                                                                                                                                                                                                                                                                                                       |
| \> \> `excludes`                   | `List<String>`                   | `[]`               | 预编译器所忽略的包名或类名。                                                                                                                                                                                                                                                                                                                                |
| \> `entry`                         |                                  |                    | Jimmer 实体聚合类的类名配置。                                                                                                                                                                                                                                                                                                                            |
| \> \> `objects`                    | `String`                         | `Objects`          |                                                                                                                                                                                                                                                                                                                                               |
| \> \> `tables`                     | `String`                         | `Tables`           |                                                                                                                                                                                                                                                                                                                                               |
| \> \> `tableExes`                  | `String`                         | `TableExes`        |                                                                                                                                                                                                                                                                                                                                               |
| \> \> `fetchers`                   | `String`                         | `Fetchers`         |                                                                                                                                                                                                                                                                                                                                               |

