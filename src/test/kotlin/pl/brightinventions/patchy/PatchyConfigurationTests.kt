package pl.brightinventions.patchy

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.web.HttpMessageConvertersAutoConfiguration
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import javax.validation.Valid
import javax.validation.constraints.NotNull


private object EditedObject {
    var name = "default"
}

class EditObjectRequest : PatchyRequest {
    override var _changes = mapOf<String, Any?>()

    @get:NotNull
    val name: String? by { _changes }
}

@Configuration
@Import(PatchyConfiguration::class)
@ImportAutoConfiguration(HttpMessageConvertersAutoConfiguration::class)
@ComponentScan
open class TestsConfiguration {

}

@WebAppConfiguration
@SpringApplicationConfiguration(classes = arrayOf(TestsConfiguration::class))
@RunWith(SpringJUnit4ClassRunner::class)
open class TestAppTests {
    @Autowired
    lateinit var context: WebApplicationContext

    val mockMvc: MockMvc by lazy {
        MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `rejects invalid request`() {
        testRequest(mapOf("name" to null)).andExpect {
            it.response.status.assert.isEqualTo(400)
        }
    }

    @Test
    fun `accept valid request`() {
        testRequest(mapOf("name" to "updated")).andExpect {
            it.response.status.assert.isEqualTo(200)
            EditedObject.name.assert.isEqualTo("updated")
        }
    }

    private fun testRequest(content: Map<String, Any?>): ResultActions {
        return mockMvc.perform {
            MockMvcRequestBuilders.patch("/test")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ObjectMapper().writeValueAsBytes(content))
                    .buildRequest(it)
        }
    }

}

@RestController
class TestController {

    @RequestMapping(path = arrayOf("/test"), method = arrayOf(RequestMethod.PATCH))
    fun update(@Valid request: EditObjectRequest) {
        request.applyChangesTo(EditedObject)
    }

}

