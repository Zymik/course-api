package ru.kosolapov.course.user.exception.register

import ru.kosolapov.course.exception.BadRequestException

class BadPasswordException(message: String) : BadRequestException(message)