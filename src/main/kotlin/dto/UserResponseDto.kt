package dto

import model.UserModel

class UserResponseDto(
    val id: Int,
    val userName: String
) {
    companion object{
        fun fromModel(model: UserModel) = UserResponseDto(
            id = model.id,
            userName = model.userName
        )
    }
}