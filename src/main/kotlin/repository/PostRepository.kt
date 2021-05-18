package repository

import model.PostModel
import model.UserModel

interface PostRepository {
    suspend fun getAll(): List<PostModel>
    suspend fun getById(id: Int): PostModel?
    suspend fun save(item: PostModel): PostModel
    suspend fun removeById(id: Int)
    suspend fun likeById(id: Int, userId: Int): PostModel?
    suspend fun dislikeById(id: Int, userId: Int): PostModel?

}
