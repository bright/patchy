package pl.brightinventions.patchy

import org.springframework.beans.BeanWrapperImpl

interface PatchyRequest {
    var _changes: Map<String, Any?>
}

val PatchyRequest.declaredChanges: Map<String,Any?> get() = BeanWrapperImpl(this).let { bean ->
    val declaredProperties = bean.propertyDescriptors.map { it.name }.toSet()
    _changes.filterKeys { declaredProperties.contains(it) }
}