package repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import model.UserModel

class UserRepositoryInMemoryWithMutexImpl : UserRepository {
    private var nextId = 1
    private val items = mutableListOf<UserModel>()
    private val mutex = Mutex()

    override suspend fun getAll(): List<UserModel> {
        mutex.withLock {
            return items.toList()
        }
    }

    override suspend fun getById(id: Int): UserModel? {
        mutex.withLock {
            return items.find { it.id == id }
        }
    }

    override suspend fun getByIds(ids: Collection<Int>): List<UserModel> {
        mutex.withLock {
            return items.filter { ids.contains(it.id) }
        }
    }

    override suspend fun getByUsername(username: String): UserModel? {
        mutex.withLock {
            return items.find { it.userName == username }
        }
    }

    override suspend fun add(item: UserModel): UserModel {
        mutex.withLock {
            val copy = item.copy(id = nextId++)
            items.add(copy)
            return copy
        }
    }

    override suspend fun save(item: UserModel): UserModel =
        mutex.withLock {
            when(val index = items.indexOfFirst { it.id == item.id }) {
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

}