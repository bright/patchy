package pl.brightinventions.patchy

import org.junit.gen5.api.Test
import org.junit.gen5.junit4.runner.JUnit5
import org.junit.runner.RunWith


private class TestRequest : PatchyRequest {
    override var _changes = mapOf<String, Any?>()

    val name: String by { _changes }
    val age: Int by { _changes }
}

private class Target {
    var name = "default"
    var age = 0
    var role = "default"
}

@RunWith(JUnit5::class)
class AutoApplyPropertyChangesTests {
    private val target = Target()
    private val request = TestRequest()
    @Test
    fun `empty request results in no changes`() {
        request.applyChangesTo(target)

        target.assert.isEqualToComparingFieldByField(Target())
    }

    @Test
    fun `changes only properties present in the request`() {
        request._changes = mapOf("age" to 12)

        request.applyChangesTo(target)

        target.age.assert.isEqualTo(12)
        target.name.assert.isEqualTo(Target().name)
    }

    @Test
    fun `ignores changes not declared in request`(){
        request._changes = mapOf("name" to "updated", "role" to "newRole")

        request.applyChangesTo(target)

        target.name.assert.isEqualTo("updated")
        target.role.assert.isEqualTo(Target().role)
    }
}


