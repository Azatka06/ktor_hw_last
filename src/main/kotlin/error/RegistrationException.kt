package error

class RegistrationException(massage: String? = "User already exists") : Exception(massage)