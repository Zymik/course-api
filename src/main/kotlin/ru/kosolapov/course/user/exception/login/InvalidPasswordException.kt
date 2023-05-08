package ru.kosolapov.course.user.exception.login

import ru.kosolapov.course.exception.BadRequestException

class InvalidPasswordException(val name: String) : BadRequestException("Invalid password for user with name $name")