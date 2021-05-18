package repository

import error.AlreadyLikedException
import error.NotLikedYetException
import error.PostNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.*
import kotlin.coroutines.EmptyCoroutineContext

class PostRepositoryMutexImpl : PostRepository {
    private var nextId = 1
    private val items = mutableListOf<PostModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<PostModel> = mutex.withLock {
        val newItems = items.map { it.copy(timesShown = it.timesShown + 1) }
        items.clear()
        items.addAll(newItems)
        items.reversed()
    }

    override suspend fun getById(id: Int): PostModel? = mutex.withLock {
        when (val index = items.indexOfFirst { it.id == id }) {
            -1 -> null
            else -> {
                val item = items[index]
                val newItem = item.copy(timesShown = item.timesShown + 1)
                items[index] = newItem
                newItem
            }
        }
    }

    override suspend fun save(item: PostModel): PostModel = mutex.withLock {
        when (val index = items.indexOfFirst { it.id == item.id }) {
            -1 -> {
                val copy = item.copy(id = nextId++)
                items.add(copy)
                copy
            }
            else -> {
                items[index] = item
                item
            }
        }
    }

    override suspend fun removeById(id: Int) {
        mutex.withLock {
            items.removeIf { it.id == id }
        }
    }

    override suspend fun likeById(id: Int, userId: Int): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    if (!item.likedByMe.contains(userId)) {
                        item.likedByMe.add(userId)
                        val newItem = item.copy(likesCount = item.likesCount + 1, likedByMe = item.likedByMe)
                        items[index] = newItem
                        newItem
                    } else throw AlreadyLikedException()
                }
            }
        }
    }


    override suspend fun dislikeById(id: Int, userId: Int): PostModel? {
        mutex.withLock {
            return when (val index = items.indexOfFirst { it.id == id }) {
                -1 -> null
                else -> {
                    val item = items[index]
                    if (!item.likedByMe.contains(userId)) {
                        item.likedByMe.remove(userId)
                        val newItem = item.copy(likesCount = item.likesCount - 1, likedByMe = item.likedByMe)
                        items[index] = newItem
                        newItem
                    } else throw NotLikedYetException()
                }
            }
        }
    }

    private fun generateContent(): List<PostModel> {

        val timeMillis = System.currentTimeMillis()

        val postsList = mutableListOf(
            PostModel(
                1,
                "A.R. Sagutdinov",
                "Something",
                timeMillis + 5_000,
                likesCount = 1,
                shareCount = 2
            ),
            PostModel(
                2,
                "Tarzan from Africa",
                "AAAAAaaaaAAAAAaaaAAAAaaa!!!!",
                timeMillis + 10_000,
                likesCount = 10,
                video = Video("https://www.youtube.com/watch?v=rxr3OzyGcyE"),
                postType = PostType.VIDEO_POST
            ),
            PostModel(
                3,
                "Shaverma",
                "12:00 free 10 minutes",
                timeMillis - 5_000,
                shareCount = 2,
                address = "Санкт-Петербург, Коменданский пр.",
                location = 60.012878 x 30.252335,
                postType = PostType.EVENT_POST
            ),
            PostModel(
                4,
                "WarMan",
                "The World is mine!!!",
                timeMillis - 10_000,
                likesCount = 5,
                shareCount = 2
            ),
            PostModel(
                5,
                "Brutal BatMan",
                "Rock in my life!",
                timeMillis,
                likesCount = 100,
                shareCount = 2,
                advertising = Advertising(
                    "https://static-ru.insales.ru/images/products/1/6335/205330623/460c2a624899693ea071e424032b89c5572eaa0a.jpg",
                    "https://www.arhybes.com/video-batmetal"
                ),
                postType = PostType.ADVERTISING
            )
        )

//        postsList.add(
//            PostModel(
//                6,
//                "CrazyMan",
//                "I want to see the world in the fire!!!",
//                timeMillis + 20_000,
//                likesCount = 2,
//                source = postsList[4],
//                postType = PostType.REPOST
//            )
//        )
        return postsList
    }

    fun main() {

        CoroutineScope(EmptyCoroutineContext).launch {
            mutex.withLock {
                items.addAll(generateContent())
            }
        }

    }
}

