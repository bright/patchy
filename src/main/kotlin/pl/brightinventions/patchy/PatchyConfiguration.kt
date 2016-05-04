package pl.brightinventions.patchy

import org.springframework.boot.autoconfigure.web.HttpMessageConverters
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@Configuration
@EnableWebMvc
open class PatchyConfiguration {
    @Bean
    open fun patchyMvcSupport(messageConverters: HttpMessageConverters): WebMvcConfigurer =
            PatchyMvcConfigurer(messageConverters)
}

internal class PatchyMvcConfigurer(val messageConverters: HttpMessageConverters) : WebMvcConfigurerAdapter() {
    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers += PatchyRequestHandlerMethodArgumentResolver(messageConverters.toList(), null)
    }
}
