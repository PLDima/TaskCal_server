package inc.dimasic.taskcal.backend

import inc.dimasic.taskcal.backend.api.dto.UserDto
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AuthRoutesTest {

    @Test
    fun `registering the same token twice returns the same user`() = apiTest {
        val token = randomToken()
        val first = register(token).assertStatus(200).body<UserDto>()
        val second = register(token).assertStatus(200).body<UserDto>()
        assertEquals(first.userId, second.userId)

        val me = client.get("/api/v1/me") { bearerAuth(token) }.assertStatus(200).body<UserDto>()
        assertEquals(first.userId, me.userId)
    }

    @Test
    fun `different tokens get different users`() = apiTest {
        val a = register(randomToken()).body<UserDto>()
        val b = register(randomToken()).body<UserDto>()
        assertNotEquals(a.userId, b.userId)
    }

    @Test
    fun `short or blank tokens are rejected`() = apiTest {
        register("too-short").assertStatus(400)
        register(" ".repeat(40)).assertStatus(400)
    }

    @Test
    fun `missing or unknown token is unauthorized`() = apiTest {
        client.get("/api/v1/me").assertStatus(401)
        client.get("/api/v1/tasks") { bearerAuth(randomToken()) }.assertStatus(401)
    }

    @Test
    fun `health needs no token`() = apiTest {
        client.get("/health").assertStatus(200)
    }
}
