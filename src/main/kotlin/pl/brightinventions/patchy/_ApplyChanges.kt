package pl.brightinventions.patchy

import org.springframework.beans.BeanWrapperImpl

fun PatchyRequest.applyChangesTo(target: Any) {
    val targetBean = BeanWrapperImpl(target)
    declaredChanges.forEach { change ->
        if (targetBean.isWritableProperty(change.key)) {
            targetBean.setPropertyValue(change.key, targetBean.convertForProperty(change.value, change.key))
        }
    }
}