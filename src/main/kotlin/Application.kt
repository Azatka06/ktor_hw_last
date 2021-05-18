import error.ForbiddenException
import error.RegistrationException
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import kotlinx.coroutines.runBlocking
import org.kodein.di.generic.*
import org.kodein.di.ktor.KodeinFeature
import org.kodein.di.ktor.kodein
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import repository.PostRepository
import repository.PostRepositoryMutexImpl
import repository.UserRepository
import repository.UserRepositoryInMemoryWithMutexImpl
import route.RoutingV1
import service.FileService
import service.JWTTokenService
import service.PostService
import service.UserService
import javax.naming.ConfigurationException


fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module(testing: Boolean = false) {
    println(testing)

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }

    install(StatusPages) {
        exception<ParameterConversionException> { e ->
            call.respond(HttpStatusCode.BadRequest)
            throw e
        }
        exception<NotFoundException> { e ->
            call.respond(HttpStatusCode.NotFound)
            throw e
        }
        exception<Throwable> { e ->
            call.respond(HttpStatusCode.InternalServerError)
            throw e
        }
        exception<NotImplementedError> { e ->
            call.respond(HttpStatusCode.NotImplemented, Error("Error"))
            throw e
        }
        exception<ForbiddenException> { e ->
            call.respond(HttpStatusCode.Forbidden)
            throw e
        }
        exception<RegistrationException> { e ->
            call.respond(HttpStatusCode.BadRequest, Error("Пользователь с таким логином уже существует!"))
            throw e
        }
    }

    install(KodeinFeature) {
        constant(tag = "upload-dir") with (
                environment.config.propertyOrNull("ktor.ncraft.upload.dir")?.getString()
                    ?: throw ConfigurationException("Upload dir is not specified")
                )
        constant(tag = "token-life-time") with (
                environment.config.propertyOrNull("ktor.ncraft.tokenLifeTime")?.getString()?.toLongOrNull()
                    ?: throw ConfigurationException("Upload dir is not specified")
                )
        constant(tag = "secret") with (
                environment.config.propertyOrNull("ktor.ncraft.secret")?.getString()
                    ?: throw ConfigurationException("Secret is not specified"))
        bind<PostRepository>() with singleton {
            PostRepositoryMutexImpl().apply {
                runBlocking {
                    main()
                }
            }
        }
        bind<UserRepository>() with eagerSingleton { UserRepositoryInMemoryWithMutexImpl() }

        bind<PostService>() with eagerSingleton { PostService(instance()) }
        bind<FileService>() with eagerSingleton { FileService(instance(tag = "upload-dir")) }
        bind<UserService>() with eagerSingleton {
            UserService(
                repo = instance(),
                tokenService = instance(),
                passwordEncoder = instance()
            ).apply {
                runBlocking {
                    this@apply.saveNewModel(userName = "sag-azat@ya.ru", password = "AZatka061195")
                }
            }
        }
        bind<JWTTokenService>() with eagerSingleton {
            JWTTokenService(
                instance("token-life-time"),
                instance("secret")
            )
        }
        bind<PasswordEncoder>() with eagerSingleton { BCryptPasswordEncoder() }
        bind<RoutingV1>() with eagerSingleton {
            RoutingV1(
                staticPath = instance(tag = "upload-dir"),
                repo = instance(),
                fileService = instance(),
                userService = instance(),
                postService = instance()
            )
        }
    }

    install(Authentication) {
        jwt {
            val jwtService by kodein().instance<JWTTokenService>()
            verifier(jwtService.verifier)
            val userService by kodein().instance<UserService>()

            validate {
                val id = it.payload.getClaim("id").asInt()
                userService.getModelById(id)
            }
        }
    }

    install(Routing) {
        val routingV1 by kodein().instance<RoutingV1>()
        routingV1.setup(this)
    }
}
