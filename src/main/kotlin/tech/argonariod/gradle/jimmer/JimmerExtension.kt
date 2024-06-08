package tech.argonariod.gradle.jimmer

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

abstract class JimmerExtension {
    /**
     * This property follows the rules defined in the ["Declaring Versions"](https://docs.gradle.org/current/userguide/single_versions.html) section of the Gradle documentation.
     * Therefore, when this property is set to `"latest.release"` or `"+"`,
     * your project will be configured to depend on the latest version of Jimmer from your configured repository.
     */
    abstract val version: Property<String>
    abstract val language: Property<JimmerLanguage>

    abstract val disableVersionNotSetWarning: Property<Boolean>

    /**
     * This property will make the `org.babyfish.jimmer:jimmer-sql` and `org.babyfish.jimmer:jimmer-sql-kotlin` dependencies to be `compileOnly` instead of `implementation`,
     * and add the `org.babyfish.jimmer:jimmer-core` or `org.babyfish.jimmer:jimmer-core-kotlin` dependency to `implementation` configuration.
     */
    abstract val ormCompileOnly: Property<Boolean>

    /**
     * This property follows the rules defined in the ["Declaring Versions"](https://docs.gradle.org/current/userguide/single_versions.html) section of the Gradle documentation.
     * Therefore, when this property is set to `"latest.release"` or `"+"`,
     * your project will be configured to depend on the latest version of quarkus-jimmer from your configured repository.
     *
     * ## WARNING!!!
     * After setting this property, the `io.github.flynndi:quarkus-jimmer` extension will be enabled,
     * which is neither an official jimmer nor quarkus-provided extension.
     *
     * This extension is in a rapid iterative development stage and may encounter issues during use.
     *
     * For more detailed information, please refer to [its GitHub repository page](https://github.com/flynndi/quarkus-jimmer-extension).
     */
    abstract val quarkusExtensionVersion: Property<String>

    @get:Nested
    abstract val immutable: JimmerImmutableConfiguration
    @Suppress("unused")
    fun immutable(action: Action<in JimmerImmutableConfiguration>) = action.execute(immutable)

    @get:Nested
    abstract val dto: JimmerDtoConfiguration
    @Suppress("unused")
    fun dto(action: Action<in JimmerDtoConfiguration>) = action.execute(dto)

    @get:Nested
    abstract val client: JimmerClientConfiguration
    @Suppress("unused")
    fun client(action: Action<in JimmerClientConfiguration>) = action.execute(client)

    @get:Nested
    abstract val java: JimmerJavaOnlyConfiguration
    @Suppress("unused")
    fun java(action: Action<JimmerJavaOnlyConfiguration>) = action.execute(java)
}

enum class JimmerLanguage {
    JAVA,
    KOTLIN,

    /**
     * Both Java and Kotlin.
     */
    BOTH;
}

abstract class JimmerImmutableConfiguration {
    /**
     * Whether generate `JimmerModule` class or not, default to `false`.
     *
     * This argument is only available for Kotlin, and the `JimmerModule` class is only needed for compatibility with version 0.7.47 and earlier.
     */
    abstract val generateJimmerModule: Property<Boolean>
}

abstract class JimmerDtoConfiguration {
    /**
     * Whether the dto properties are mutable or not, default to `false`.
     *
     * This argument is only available for Kotlin, dto properties are always mutable in Java.
     */
    abstract val mutable: Property<Boolean>

    /**
     * Directories of jimmer-dto source files, default to `["src/main/dto"]`
     */
    abstract val dirs: ListProperty<String>

    /**
     * Directories of jimmer-dto test source files, default to `["src/test/dto"]`
     */
    abstract val testDirs: ListProperty<String>

    /**
     * Default handling mode of nullable input properties.
     *
     * See details in [Handle Null Values](https://babyfish-ct.github.io/jimmer-doc/docs/mutation/save-command/input-dto/null-handling/#higher-level-configurations).
     */
    abstract val defaultNullableInputModifier: Property<JimmerDtoNullableInputModifier>

    /**
     * Input DTO with Dynamic fields will implement `org.hibernate.validator.Incubating.HibernateValidatorEnhancedBean` interface,
     * default to false.
     *
     * See details in [babyfish-ct/jimmer#561](https://github.com/babyfish-ct/jimmer/issues/561).
     */
    abstract val hibernateValidatorEnhancement: Property<Boolean>
}

@Suppress("unused")
enum class JimmerDtoNullableInputModifier(internal val value: String) {
    FIXED("fixed"),
    STATIC("static"),
    DYNAMIC("dynamic"),
    FUZZY("fuzzy")
}

abstract class JimmerClientConfiguration {
    /**
     * Use embedded swagger-ui js and css files instead of referencing from unpkg cdn.
     *
     * Enable this option will cause the packaged application to be larger, but it can be used when you have trouble with accessing unpkg cdn.
     */
    abstract val enableEmbeddedSwaggerUi: Property<Boolean>
    abstract val checkedException: Property<Boolean>
    abstract val ignoreJdkWarning: Property<Boolean>
}

abstract class JimmerJavaOnlyConfiguration {
    @get:Nested
    abstract val source: JimmerJavaSourceConfiguration
    @Suppress("unused")
    fun source(action: Action<JimmerJavaSourceConfiguration>) = action.execute(source)

    /**
     * Whether keep `is` prefix of boolean props or not, default to `false`
     */
    abstract val keepIsPrefix: Property<Boolean>

    @get:Nested
    abstract val entry: JimmerJavaEntryConfiguration

    /**
     * Class name configuration of composite jimmer entities.
     */
    @Suppress("unused")
    fun entry(action: Action<JimmerJavaEntryConfiguration>) = action.execute(entry)
}

abstract class JimmerJavaSourceConfiguration {
    /**
     * full package name or type element name like `"foo.bar","foo.bar.Test"`
     */
    abstract val includes: ListProperty<String>

    /**
     * full package name or type element name like `"foo.bar","foo.bar.Test"`
     */
    abstract val excludes: ListProperty<String>
}

abstract class JimmerJavaEntryConfiguration {
    abstract val objects: Property<String>
    abstract val tables: Property<String>
    abstract val tableExes: Property<String>
    abstract val fetchers: Property<String>
}

internal fun Project.getJimmerVersion(jimmerExtension: JimmerExtension): String? {
    val result = jimmerExtension.version.orNull
    if (result == null && !jimmerExtension.disableVersionNotSetWarning.getOrElse(false)) {
        logger.warn("[tech.argonariod.gradle-plugin-jimmer] jimmer.version for Project(path=\"$path\",name=\"$name\") is not set. Skipping Jimmer configuration.")
    }
    return result
}