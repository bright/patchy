# patchy
A Spring method argument resolver that is suitable for [PATCH](https://tools.ietf.org/html/rfc5789) handling combined with validation.

This simple library let's you apply client provided changes to an entity while at the same time keep the benefits of request validation:

```
class Request : PatchyRequest {
    @get:NotBlank
    val firstName:String? by { _changes }

    @get:NotBlank
    val lastName:String? by { _changes }
    
    override var _changes = mapOf<String,Any?>()
}

@RestController
class PatchingCtrl {
    @RequestMapping("/", method = arrayOf(RequestMethod.PATCH))
    fun update(@Valid request: Request){
        request.applyChangesTo(entity)
    }
}
```

## Setup

`build.gradle`:

```
repositories {
    jcenter()
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'com.github.bright:patchy:0.1.0'
    ...
}
```

Then import the configuration with: 
```
@SpringBootApplication
@EnableWebMvc
@Import(PatchyConfiguration::class)
open class Application {
```



