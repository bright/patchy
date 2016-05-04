package pl.brightinventions.patchy.sample

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.RestController


@RestController
class PatchingCtrl {

}

@SpringBootApplication
open class Application {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val ctx = SpringApplication.run(Application::class.java, *args)
        }
    }
}