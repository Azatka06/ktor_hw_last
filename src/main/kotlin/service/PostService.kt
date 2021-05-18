package service

import dto.PostRequestDto
import dto.PostResponseDto
import dto.PostsCreatedBeforeRequestDto
import dto.RepostRequestDto
import error.ForbiddenException
import io.ktor.features.*
import model.PostModel
import model.PostType
import model.UserModel
import repository.PostRepository

class PostService(
    val repo: PostRepository
) {
    suspend fun getAll(): List<PostResponseDto> = repo.getAll().map(PostResponseDto.Companion::fromModel)

    suspend fun getByID(id: Int): PostResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return PostResponseDto.fromModel(model)
    }

    suspend fun save(request: PostRequestDto, user: UserModel): PostResponseDto {
        if (user.userName != request.author) { //TODO реализовать через id
            throw ForbiddenException("Невозможно редактировать!")
        }
        val model = repo.save(
            PostRequestDto.toModel(request)
        )
        return PostResponseDto.fromModel(model)
    }

    suspend fun deleteById(id: Int, user: UserModel): PostResponseDto{
        val model = repo.getById(id) ?: throw NotFoundException()

        if (user.userName != model.author) {
            throw ForbiddenException("Невозможно удалить пост!")
        }
        val response = PostResponseDto.fromModel(model)
        repo.removeById(id)
        return response
    }

    suspend fun likeById(id: Int, userId: Int): PostResponseDto {
        val model = repo.likeById(id, userId) ?: throw NotFoundException()
        val copy = model.copy(likesCount =  model.likesCount.inc(), likedByMe = model.likedByMe)
        return PostResponseDto.fromModel(repo.save(copy))
    }

    suspend fun dislikeById(id: Int, userId: Int): PostResponseDto {
        val model = repo.dislikeById(id, userId) ?: throw NotFoundException()
        val copy = model.copy(likesCount = model.likesCount.dec(), likedByMe = model.likedByMe)
        return PostResponseDto.fromModel(repo.save(copy))
    }

    suspend fun repostById(id: Int, user: UserModel, repostRequestDto: RepostRequestDto): PostResponseDto {
        val reposted = repo.getById(id)
        val newPostForSave = PostModel(
            id = -1,
            author = user.userName,
            content = repostRequestDto.content,
            created = System.currentTimeMillis(),
            postType = PostType.REPOST,
            source = reposted
        )
        val repost = repo.save(newPostForSave)
        return PostResponseDto.fromModel(repost)
    }


}