package pl.brightinventions.patchy

import com.fasterxml.jackson.databind.ObjectMapper
import jdk.nashorn.internal.runtime.JSONListAdapter
import org.junit.gen5.api.Test
import org.junit.gen5.junit4.runner.JUnit5
import org.junit.runner.RunWith
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer
import org.springframework.web.bind.support.DefaultDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import javax.activation.MimeType
import kotlin.reflect.jvm.javaMethod

@RunWith(JUnit5::class)
class MethodArgumentResolver_supportsParameter_Tests {
    val resolver = PatchyRequestHandlerMethodArgumentResolver()
    private val methods = Methods()

    @Test
    fun `supports method with custom patchy request`() {
        resolver.supportsParameter(methods.methodWithCustomPatchyRequestParameter).assert.isEqualTo(true)
    }

    @Test
    fun `supports patchy request interface`() {
        resolver.supportsParameter(methods.methodWithPatchyRequestRequestParameter).assert.isEqualTo(true)
    }

    @Test
    fun `does not support other requests`() {
        resolver.supportsParameter(methods.methodWithNonPatchyRequestParameter).assert.isEqualTo(false)
    }


}

@Suppress("UNUSED_PARAMETER")
private class Methods {
    fun methodWithPatchyRequest(request: PatchyRequest) {
    }

    val methodWithPatchyRequestRequestParameter = MethodParameter(Methods::methodWithPatchyRequest.javaMethod, 0)

    private fun methodWithCustomPatchyRequest(request: CustomRequest) {
    }

    val methodWithCustomPatchyRequestParameter = MethodParameter(Methods::methodWithCustomPatchyRequest.javaMethod, 0)

    fun methodWithNonPatchyRequest(request: String) {
    }

    val methodWithNonPatchyRequestParameter = MethodParameter(Methods::methodWithNonPatchyRequest.javaMethod, 0)

}

@RunWith(JUnit5::class)
class MethodArgumentResolver_resolve_Tests {
    val resolver = PatchyRequestHandlerMethodArgumentResolver()
    fun request(configure: (MockHttpServletRequest) -> Any? = {}): ServletWebRequest {
        val mockHttpServletRequest = MockHttpServletRequest()
        configure(mockHttpServletRequest)
        return ServletWebRequest(mockHttpServletRequest)
    }

    @Test
    fun `throws an error if it is impossible to construct argument instance`() {
        { resolve(methods.methodWithPatchyRequestRequestParameter) }.assertThrows
    }

    @Test
    fun `constructs custom request`() {
        val resolveCustomRequest = resolveCustomRequest()

        resolveCustomRequest.assert.isNotNull()
        resolveCustomRequest!!.changes.assert.isEmpty()
    }

    @Test
    fun `fills changes with parameter map`() {
        val customRequest = resolveCustomRequest(request { r ->
            r.method = "POST"
            r.contentType = MediaType.APPLICATION_JSON_VALUE
            r.setContent(ObjectMapper().writeValueAsBytes(mapOf("name" to  "Request Name")))
        })

        customRequest!!.changes.assert.isEqualTo(mapOf("name" to  "Request Name"))
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


    fun resolveCustomRequest(request: ServletWebRequest = request())
            = resolve(methods.methodWithCustomPatchyRequestParameter, request = request) as? CustomRequest

    fun resolve(param: MethodParameter, request: ServletWebRequest = request())
            = resolver.resolveArgument(param, ModelAndViewContainer(), request, DefaultDataBinderFactory(ConfigurableWebBindingInitializer()))

    private val methods = Methods()

}

class CustomRequest(override var changes: Map<String, Any?> = mapOf()) : PatchyRequest {
    val name: String? by changes
}

