package model

import io.ktor.auth.*


data class UserModel(
    val id: Int = 1,
    val userName: String,
    val password: String
): Principal