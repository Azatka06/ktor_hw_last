package dto

import model.*

data class PostRequestDto(
    val id: Int,
    val author: String,
    val content: String,
    val created: Long,
    val likesCount: Int,
    val repostCount: Int,
    val shareCount: Int,
    val likedByMe: MutableSet<Int>,
    val repostByMe: MutableSet<Int>,
    val sharedByMe: Boolean,
    val address: String?,
    val location: Location?,
    val video: Video?,
    val advertising: Advertising?,
    val source: PostModel?,
    val postType: PostType,
    val timesShown: Long
) {
    companion object {
        fun toModel(dto: PostRequestDto) = PostModel(
            id = dto.id,
            author = dto.author,
            content = dto.content,
            created = dto.created,
            likesCount = dto.likesCount,
            repostCount = dto.repostCount,
            shareCount = dto.shareCount,
            likedByMe = dto.likedByMe,
            repostByMe = dto.repostByMe,
            sharedByMe = dto.sharedByMe,
            address = dto.address,
            location = dto.location,
            video = dto.video,
            advertising = dto.advertising,
            source = dto.source,
            postType = dto.postType,
            timesShown = dto.timesShown
        )
    }
}
