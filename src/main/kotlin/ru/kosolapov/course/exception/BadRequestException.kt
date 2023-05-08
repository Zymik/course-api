package ru.kosolapov.course.exception

import io.ktor.http.*

abstract class BadRequestException(message: String) : ClientRequestException(message, HttpStatusCode.BadRequest)