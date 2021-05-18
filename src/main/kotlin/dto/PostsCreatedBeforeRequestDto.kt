package dto

data class PostsCreatedBeforeRequestDto(
    val idCurPost: Int,
    val countUploadedPosts: Int
)