package pl.brightinventions.patchy

import kotlin.reflect.KProperty

operator fun <V, V1 : V> (() -> Map<in String, V>).getValue(thisRef: Any?, property: KProperty<*>): V1 {
    val map = this()
    val key = property.name
    @Suppress("UNCHECKED_CAST")
    val value = map[key] as V1
    if (property.returnType.isMarkedNullable) {
        return value
    } else {
        if(value != null){
            return value
        }
        if(map.containsKey(key)){
            throw KotlinNullPointerException("Property baking map returned null value for key '$key' for non nullable property: $property")
        } else {
            throw KotlinNullPointerException("Property baking map has no key '$key' for non nullable property $property")
        }
    }
}