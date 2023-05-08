package ru.kosolapov.course.exception

import io.ktor.http.*

abstract class ClientRequestException(message: String, val status: HttpStatusCode) : Exception(message)