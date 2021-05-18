
import com.jayway.jsonpath.JsonPath
import io.ktor.application.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.*
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ApplicationTest {
    private val jsonContentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)
    private val multipartBoundary = "***blob***"
    private val multipartContentType =
        ContentType.MultiPart.FormData.withParameter("boundary", multipartBoundary).toString()
    private val uploadPath = Files.createTempDirectory("test").toString()

    private fun configure(config: MapApplicationConfig.() -> Unit = {}): Application.() -> Unit = {
        (environment.config as MapApplicationConfig).apply {
            put("ktor.ncraft.upload.dir", uploadPath)
            put("ktor.ncraft.secret", "secret")
            put("ktor.ncraft.tokenLifeTime", "1000")
            config()
        }
        module()
    }



    @Test
    fun testUpload() {
        withTestApplication(configure()) {
            with(handleRequest(HttpMethod.Post, "/api/v1/media") {
                addHeader(HttpHeaders.ContentType, multipartContentType)
                addHeader(HttpHeaders.Authorization, "Bearer ${register()}")
                setBody(
                    multipartBoundary,
                    listOf(
                        PartData.FileItem({
                            Files.newInputStream(Paths.get("./src/test/resources/test.png"))
                                .asInput()
                        }, {}, headersOf(
                            HttpHeaders.ContentDisposition to listOf(
                                ContentDisposition.File.withParameter(
                                    ContentDisposition.Parameters.Name,
                                    "file"
                                ).toString(),
                                ContentDisposition.File.withParameter(
                                    ContentDisposition.Parameters.FileName,
                                    "photo.png"
                                ).toString()
                            ),
                            HttpHeaders.ContentType to listOf(ContentType.Image.PNG.toString())
                        )
                        )
                    )
                )
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("\"id\""))
            }
        }
    }

    @Test
    fun testUnauthorized() {
        withTestApplication(configure()) {
            with(handleRequest(HttpMethod.Get, "/api/v1/posts")) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
                println(response.content)
                println(response.status())
            }
        }
    }

    @Test
    fun testRegistration() {
        withTestApplication(configure()) {
            with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                addHeader(HttpHeaders.Authorization, "Bearer ${register()}")
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val username = JsonPath.read<String>(response.content!!, "$.userName")
                assertEquals("Test", username)
            }
        }
    }

    @Test
    fun testExpire() {
        withTestApplication(configure {
            put("ktor.ncraft.tokenLifeTime", "-1000")
        }){
            with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                addHeader(HttpHeaders.Authorization, "Bearer ${register()}")
            }) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun testAuth() {
        withTestApplication(configure()) {
            with(handleRequest(HttpMethod.Get, "/api/v1/me") {
                addHeader(HttpHeaders.Authorization, "Bearer ${authenticator()}")
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val username = JsonPath.read<String>(response.content!!, "$.userName")
                assertEquals("sag-azat@ya.ru", username)
            }
        }
    }

    private fun TestApplicationEngine.authenticator(): String =
        handleRequest(HttpMethod.Post, "/api/v1/authentication") {
            addHeader(HttpHeaders.ContentType, jsonContentType.toString())
            setBody(
                """
                {
                "userName": "sag-azat@ya.ru",
                "password": "AZatka061195"
                }
                """.trimIndent()
            )
        }.let {
            JsonPath.read(it.response.content!!, "$.token")
        }



    private fun TestApplicationEngine.register(): String =
        handleRequest(HttpMethod.Post, "/api/v1/registration") {
            addHeader(HttpHeaders.ContentType, jsonContentType.toString())
            setBody(
                """
                {
                "id" : 0,
                "userName": "Test",
                "password": "1qaz2wsx"
                }
                """.trimIndent()
            )
        }.let {
            JsonPath.read(it.response.content!!, "$.token")
        }
}