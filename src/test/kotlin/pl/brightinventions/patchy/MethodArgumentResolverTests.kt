package pl.brightinventions.patchy

import com.fasterxml.jackson.databind.ObjectMapper
import jdk.nashorn.internal.runtime.JSONListAdapter
import org.hibernate.validator.constraints.Length
import org.junit.gen5.api.Test
import org.junit.gen5.junit4.runner.JUnit5
import org.junit.runner.RunWith
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.validation.annotation.Validated
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer
import org.springframework.web.bind.support.DefaultDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
import javax.activation.MimeType
import javax.validation.ValidationException
import javax.validation.constraints.NotNull
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaMethod

@RunWith(JUnit5::class)
class MethodArgumentResolver_supportsParameter_Tests {
    val resolver = PatchyRequestHandlerMethodArgumentResolver()
    private val methods = Methods()

    @Test
    fun `supports method with custom patchy request`() {
        resolver.supportsParameter(methods.withCustomRequestParameter).assert.isEqualTo(true)
    }

    @Test
    fun `supports patchy request interface`() {
        resolver.supportsParameter(methods.withInterfaceParameter).assert.isEqualTo(true)
    }

    @Test
    fun `does not support other requests`() {
        resolver.supportsParameter(methods.withOtherParamParameter).assert.isEqualTo(false)
    }


}

@Suppress("UNUSED_PARAMETER")
class Methods {
    fun withInterface(request: PatchyRequest) {
    }

    val withInterfaceParameter = MethodParameter(Methods::withInterface.javaMethod, 0)

    fun withCustomRequest(request: CustomRequest) {
    }

    val withCustomRequestParameter = MethodParameter(Methods::withCustomRequest.javaMethod, 0)

    fun withCustomRequestValidated(@Validated request: CustomRequest) {
    }

    val withCustomRequestValidatedParameter = MethodParameter(Methods::withCustomRequestValidated.javaMethod, 0)

    fun withOtherParam(request: String) {
    }

    val withOtherParamParameter = MethodParameter(Methods::withOtherParam.javaMethod, 0)

}

@RunWith(JUnit5::class)
class MethodArgumentResolver_resolve_Tests : ResolverTestBase() {
    @Test
    fun `throws an error if it is impossible to construct argument instance`() {
        { resolve(methods.withInterfaceParameter) }.assertThrows
    }

    @Test
    fun `constructs custom request`() {
        val resolveCustomRequest = resolveCustomRequest()

        resolveCustomRequest.assert.isNotNull()
        resolveCustomRequest!!.changes.assert.isEmpty()
    }

    @Test
    fun `fills changes with parameter map`() {
        val body = mapOf("name" to  "Request Name")
        val customRequest = resolveCustomRequestWithBody(body)

        customRequest!!.changes.assert.isEqualTo(body)
    }

    @Test
    fun `empty body results in empty changes`() {
        val customRequest = resolveCustomRequest(request { r ->
            r.method = "POST"
            r.contentType = MediaType.APPLICATION_JSON_VALUE
            r.setContent(ByteArray(0))
        })

        customRequest!!.changes.assert.isEmpty()
    }

    @Test
    fun `validates if attribute is present in request`() {
        { resolveCustomRequestValidatedWithBody(mapOf("name" to "ab")) }.assertThrows
                .isInstanceOf(MethodArgumentNotValidException::class.java)
    }

    @Test
    fun `skips validation if attribute is not present in request`() {
        resolveCustomRequestValidatedWithBody(mapOf()).assert.isNotNull()
    }

}

open class ResolverTestBase {
    private object Support : WebMvcConfigurationSupport() {
        val webBindingInitializer = super.getConfigurableWebBindingInitializer()
    }

    val resolver = PatchyRequestHandlerMethodArgumentResolver()
    fun request(configure: (MockHttpServletRequest) -> Any? = {}): ServletWebRequest {
        val mockHttpServletRequest = MockHttpServletRequest()
        configure(mockHttpServletRequest)
        return ServletWebRequest(mockHttpServletRequest)
    }

    fun resolveCustomRequest(request: ServletWebRequest = request())
            = resolve(methods.withCustomRequestParameter, request = request) as? CustomRequest

    fun resolveCustomRequestValidated(request: ServletWebRequest = request())
            = resolve(methods.withCustomRequestValidatedParameter, request = request) as? CustomRequest

    val binderFactory = DefaultDataBinderFactory(Support.webBindingInitializer.apply { (validator as LocalValidatorFactoryBean).afterPropertiesSet() })

    fun resolve(param: MethodParameter, request: ServletWebRequest = request()): Any? {
        return resolver.resolveArgument(param, ModelAndViewContainer(), request, binderFactory)
    }

    fun resolveCustomRequestWithBody(body: Map<String, String>): CustomRequest? {
        val customRequest = resolveCustomRequest(request { r ->
            r.method = "PATCH"
            r.contentType = MediaType.APPLICATION_JSON_VALUE
            r.setContent(ObjectMapper().writeValueAsBytes(body))
        })
        return customRequest
    }

    fun resolveCustomRequestValidatedWithBody(body: Map<String, String>): CustomRequest? {
        val customRequest = resolveCustomRequestValidated(request { r ->
            r.method = "PATCH"
            r.contentType = MediaType.APPLICATION_JSON_VALUE
            r.setContent(ObjectMapper().writeValueAsBytes(body))
        })
        return customRequest
    }

    val methods = Methods()
}

@RunWith(JUnit5::class)
class TmpTest {
    @Test
    fun `delegation`() {
        val me = CustomRequest()
        println(me.name)
        me.changes = mapOf("name" to "ala")
        me.name.assert.isEqualTo("ala")
    }
}

class CustomRequest(override var changes: Map<String, Any?> = (mapOf<String, Any?>())) : PatchyRequest {
    @get:Length(min = 3)
    @get:NotNull
    val name: String? by { changes }
}

public operator fun <V, V1 : V> (() -> Map<in String, V>).getValue(thisRef: Any?, property: KProperty<*>): V1 {
    val map = this()
    val key = property.name
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


