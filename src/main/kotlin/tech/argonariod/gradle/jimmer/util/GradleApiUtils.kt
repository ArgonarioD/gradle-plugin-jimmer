package tech.argonariod.gradle.jimmer.util

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

internal inline fun <T, R> Property<T>.letNotNull(action: (T) -> R) {
    this.orNull?.let(action)
}

internal inline fun <T, R> ListProperty<T>.letNotEmpty(action: (List<T>) -> R) {
    this.orNull?.takeIf { it.isNotEmpty() }?.let(action)
}