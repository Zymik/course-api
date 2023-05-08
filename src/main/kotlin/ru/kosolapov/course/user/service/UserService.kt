package ru.kosolapov.course.user.service

import ru.kosolapov.course.user.Token
import ru.kosolapov.course.user.UserCredentials
import ru.kosolapov.course.user.UserRepository
import ru.kosolapov.course.user.exception.NoSuchUserException
import ru.kosolapov.course.user.exception.login.InvalidPasswordException
import ru.kosolapov.course.user.exception.register.BadPasswordException
import ru.kosolapov.course.user.exception.register.UserAlreadyExistException

class UserService(
    private val userRepository: UserRepository,
    private val passwordHashService: PasswordHashService,
    private val jwtService: JwtService
) {
    companion object {
        const val MIN_PASSWORD_LENGTH = 15
    }

    suspend fun registerUser(userCredentials: UserCredentials): Token {
        if (userCredentials.password.length < MIN_PASSWORD_LENGTH) {
            throw BadPasswordException("Min length of password should be $MIN_PASSWORD_LENGTH")
        }

        if (userRepository.exist(userCredentials.name)) {
            throw UserAlreadyExistException(userCredentials.name)
        }

        val hashedPassword = passwordHashService.hashPassword(userCredentials.password)
        userRepository.save(userCredentials.name, hashedPassword)

        return jwtService.generateToken(userCredentials.name)
    }

    suspend fun loginUser(userCredentials: UserCredentials): Token {
        checkUserCredentials(userCredentials)
        return jwtService.generateToken(userCredentials.name)
    }

    private suspend fun checkUserCredentials(userCredentials: UserCredentials) {
        val user =
            userRepository.getWithPassword(userCredentials.name) ?: throw NoSuchUserException(userCredentials.name)
        val expectedPassword = user.password
        val encodedInputPassword = passwordHashService.hash(userCredentials.password, user.salt)

        if (!encodedInputPassword.contentEquals(expectedPassword)) {
            throw InvalidPasswordException(userCredentials.name)
        }
    }

    suspend fun checkUserExist(name: String) {
        userRepository.get(name) ?: throw NoSuchUserException(name)
    }

}