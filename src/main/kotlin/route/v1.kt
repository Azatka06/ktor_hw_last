package route

import dto.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import model.PostModel
import model.PostType
import model.UserModel
import repository.PostRepository
import service.FileService
import service.PostService
import service.UserService

val <T: Any> PipelineContext<T, ApplicationCall>.id
    get() = call.parameters["id"]?.toIntOrNull() ?: throw ParameterConversionException("id", "Long")

val <T: Any> PipelineContext<T, ApplicationCall>.me
    get() = call.authentication.principal<UserModel>()

class RoutingV1(
    private val staticPath: String,
    private val repo: PostRepository,
    private val fileService: FileService,
    private val userService: UserService,
    private val postService: PostService
) {


    fun setup(configuration: Routing) {
        with(configuration) {

            route("/api/v1/") {
                static("/static") {
                    files(staticPath)
                }

                route("/") {
                    post("/registration") {
                        val input = call.receive<RegistrationRequestDto>()
                        val response = userService.register(input)
                        call.respond(response)
                    }
                    post("/authentication") {
                        val input = call.receive<AuthenticationRequestDto>()
                        val response = userService.authenticate(input)
                        call.respond(response)
                    }
                }

                authenticate {
                    route("/me") {
                        get {
                            val me = call.authentication.principal<UserModel>()
                            call.respond(UserResponseDto.fromModel(me!!))
                        }
                    }

                    route("/media") {
                        post {
                            val multipart = call.receiveMultipart()
                            val response = fileService.save(multipart)
                            call.respond(response)
                        }
                    }

                    route("/posts") {
                        get {
                            call.respond(postService.getAll())
                        }
                        get("/{id}") {
                            call.respond(postService.getByID(id))
                        }
                        post {
                            val request = call.receive<PostRequestDto>()
                            call.respond(postService.save(request, me!!))
                        }
                        delete("/{id}") {
                            call.respond(postService.deleteById(id, me!!))
                        }
                        put("/{id}/likes") {
                            call.respond(postService.likeById(id, me!!.id))
                        }
                        put("/{id}/dislike") {
                            call.respond(postService.dislikeById(id, me!!.id))
                        }


                        post("/{id}/reposts") {
                            val id =
                                call.parameters["id"]?.toIntOrNull() ?: throw ParameterConversionException(
                                    "id",
                                    "Int"
                                )
                            val repostRequestDto = call.receive<RepostRequestDto>()
                            call.respond(postService.repostById(id, me!!, repostRequestDto))
                        }

                    }

                    route("/share") {
                        get("/{id}") {
                            val model = repo.getById(id) ?: throw NotFoundException()
                            val response = PostResponseDto.fromModel(model)
                            call.respond(response)
                        }
                    }
                }
            }
        }
    }
}
