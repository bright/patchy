package pl.brightinventions.patchy

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.HttpMessageConverters
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@Configuration
@EnableWebMvc
open class PatchyConfiguration : WebMvcConfigurerAdapter() {
    @Autowired
    lateinit var messageConverters : HttpMessageConverters

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers += PatchyRequestHandlerMethodArgumentResolver(messageConverters.toList(), null)
    }
}