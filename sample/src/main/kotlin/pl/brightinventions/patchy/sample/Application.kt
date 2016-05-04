package pl.brightinventions.patchy.sample

import org.hibernate.validator.constraints.NotBlank
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import pl.brightinventions.patchy.PatchyConfiguration
import pl.brightinventions.patchy.PatchyRequest
import pl.brightinventions.patchy.getValue
import javax.validation.Valid


data class Request(override var _changes:Map<String,Any?> = mapOf<String,Any?>()) : PatchyRequest {
    @get:NotBlank
    val name:String? by { _changes }
}

@RestController
class PatchingCtrl {
    @RequestMapping("/", method = arrayOf(RequestMethod.PATCH))
    fun update(@Valid request: Request){
        println(request)
    }
}

@SpringBootApplication
@EnableWebMvc
@Import(PatchyConfiguration::class)
open class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val ctx = SpringApplication.run(Application::class.java, *args)
        }
    }
}