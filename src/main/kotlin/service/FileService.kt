package service

import dto.MediaResponseDto
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.MediaType
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class FileService(private val uploadPath: String) {

    private val images = listOf(ContentType.Image.JPEG, ContentType.Image.PNG)

    init {
        val path = Paths.get(uploadPath)
        if (Files.notExists(path)) {
            Files.createDirectory(path)
        }
    }

    suspend fun save(multipart: MultiPartData): MediaResponseDto {
        var response: MediaResponseDto? = null
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    if (part.name == "file") {
                        // TODO: use Apache Tika for content detection
                        if (!images.contains(part.contentType)) {
                            throw UnsupportedMediaTypeException(part.contentType ?: ContentType.Any)
                        }
                        val ext = File(part.originalFileName).extension
                        val name = "${UUID.randomUUID()}.$ext"
                        val path = Paths.get(uploadPath, name)
                        part.streamProvider().use {
                            withContext(Dispatchers.IO) {
                                Files.copy(it, path)
                            }
                        }
                        part.dispose()
                        response = MediaResponseDto(name, MediaType.IMAGE)
                        return@forEachPart
                    }
                }
            }
            part.dispose
        }
        return  response ?: throw BadRequestException("Not file field in request")
    }
}