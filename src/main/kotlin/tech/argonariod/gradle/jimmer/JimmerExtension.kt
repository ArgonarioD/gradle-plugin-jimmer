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

    /**
     * `io.github.flynndi:quarkus-jimmer` the unofficial Quarkus Jimmer extension is not available for Kotlin now.
     * So when the language is set to [JimmerLanguage.KOTLIN], this property will take no effect.
     *
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

internal fun Project.getJimmerVersion(jimmerExtension: JimmerExtension): String? {
    val result = jimmerExtension.version.orNull
    if (result == null) {
        logger.warn("[tech.argonariod.gradle-plugin-jimmer] jimmer.version is not set. Skipping Jimmer configuration.")
    }
    return result
}