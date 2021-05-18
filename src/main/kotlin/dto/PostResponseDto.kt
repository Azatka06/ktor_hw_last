package dto

import model.*

data class PostResponseDto(
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
        fun fromModel(model: PostModel) = PostResponseDto(
            id = model.id,
            author = model.author,
            content = model.content,
            created = model.created,
            likesCount = model.likesCount,
            repostCount = model.repostCount,
            shareCount = model.shareCount,
            likedByMe = model.likedByMe,
            repostByMe = model.repostByMe,
            sharedByMe = model.sharedByMe,
            address = model.address,
            location = model.location,
            video = model.video,
            advertising = model.advertising,
            source = model.source,
            postType = model.postType,
            timesShown = model.timesShown

        )
    }
}
