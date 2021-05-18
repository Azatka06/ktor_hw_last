package error

class PasswordChangeException(message: String? = "Wrong password!") : Exception(message)