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

### Spring Boot 项目

如果你的项目中存在任何工件 ID 的开头为 `spring-boot-starter`
的依赖项，那么 `org.babyfish.jimmer:jimmer-spring-boot-starter` 会代替上文中的 `jimmer-sql` 和 `jimmer-sql-kotlin`
作为依赖项被添加到你的项目中。

## 所有可配置项

本节仅列出所有可配置项，每项的详细信息请自行查看其文档注释。

```groovy
// build.gradle
import tech.argonariod.gradle.jimmer.JimmerLanguage

jimmer {
    version = 'latest.release'
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