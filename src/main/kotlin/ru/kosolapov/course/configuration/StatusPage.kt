package ru.kosolapov.course.configuration

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import ru.kosolapov.course.exception.ClientRequestException

fun Application.configureStatusPage() {
    install(StatusPages) {
        exception<ClientRequestException> { call, cause ->
            call.respondText(text = cause.message ?: "", status = cause.status)
        }

        exception<MissingRequestParameterException> { call, cause ->
            call.respondText(text = cause.localizedMessage, status = HttpStatusCode.BadRequest)
        }

        exception<ParameterConversionException> { call, cause ->
            call.respondText(text = cause.localizedMessage, status = HttpStatusCode.BadRequest)
        }

        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
}