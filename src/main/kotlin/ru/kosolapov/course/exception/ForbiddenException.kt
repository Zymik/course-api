package ru.kosolapov.course.exception

import io.ktor.http.*

abstract class ForbiddenException(message: String) : ClientRequestException(message, HttpStatusCode.Forbidden)