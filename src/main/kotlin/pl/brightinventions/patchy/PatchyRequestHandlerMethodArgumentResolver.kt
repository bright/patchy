package pl.brightinventions.patchy

import org.springframework.core.Conventions
import org.springframework.core.MethodParameter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.primaryConstructor

class PatchyRequestHandlerMethodArgumentResolver(
        converters: List<HttpMessageConverter<*>>,
        requestsResponseBodyAdvice: List<Any>?
) : AbstractMessageConverterMethodArgumentResolver(converters, requestsResponseBodyAdvice) {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return PatchyRequest::class.java.isAssignableFrom(parameter.parameterType)
    }

    constructor() : this(DefaultWebMvcConfigurationSupport.defaultMessageConverters, null)

    var requestInstanceFactory: (Class<PatchyRequest>) -> PatchyRequest = { type -> Factory.constructPatchyRequest(type) }

    @Suppress("UNCHECKED_CAST")
    override fun resolveArgument(parameter: MethodParameter,
                                 mavContainer: ModelAndViewContainer,
                                 webRequest: NativeWebRequest,
                                 binderFactory: WebDataBinderFactory): Any? {

        val result = requestInstanceFactory(parameter.parameterType as Class<PatchyRequest>)

        val attributesFromRequest = readWithMessageConverters<Map<String, Any?>>(webRequest, parameter, hashMapOf<String, Any?>().javaClass)

        result.changes = (attributesFromRequest as Map<String, Any?>? ?: emptyMap()).withDefault { null }

        val name = Conventions.getVariableNameForParameter(parameter)

        val binder = binderFactory.createBinder(webRequest, result, name)

        validateIfApplicable(binder, parameter)

        val bindingResult = binder.bindingResult.let { source ->
            BeanPropertyBindingResult(source.target, source.objectName).apply {
                source.allErrors.filter { e ->
                    when (e) {
                        is FieldError -> attributesFromRequest?.containsKey(e.field)
                        else -> true
                    }
                }.forEach { e -> addError(e) }
            }
        }

        if (bindingResult.hasErrors() && isBindExceptionRequired(binder, parameter)) {
            throw MethodArgumentNotValidException(parameter, bindingResult)
        }

        mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, bindingResult)

        return result
    }

    private object Factory {
        fun constructPatchyRequest(type: Class<PatchyRequest>): PatchyRequest {
            val primary = type.kotlin.primaryConstructor
            if (primary == null) {
                return type.newInstance()
            } else
                primary.isAccessible = true
            return primary.callBy(mapOf())
        }
    }

    private object DefaultWebMvcConfigurationSupport : WebMvcConfigurationSupport() {
        val defaultMessageConverters: List<HttpMessageConverter<*>> = super.getMessageConverters()
    }

}