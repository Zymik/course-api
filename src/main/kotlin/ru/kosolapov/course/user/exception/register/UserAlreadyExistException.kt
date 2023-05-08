package ru.kosolapov.course.user.exception.register

import ru.kosolapov.course.exception.BadRequestException

class UserAlreadyExistException(name: String) : BadRequestException("User with name $name already exist")