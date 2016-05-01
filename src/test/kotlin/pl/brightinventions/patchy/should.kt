package pl.brightinventions.patchy

import org.assertj.core.api.AbstractObjectAssert
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.MapAssert

val <T : Any?> T.assert: AbstractObjectAssert<*, T> get() = Assertions.assertThat(this)
val <T : Map<TKey,TValue>?, TKey, TValue> T.assert: MapAssert<TKey, TValue> get() = Assertions.assertThat(this)

val (() -> Any?).assertThrows: AbstractThrowableAssert<*, out Throwable>
    get() = Assertions.assertThatThrownBy { this() }