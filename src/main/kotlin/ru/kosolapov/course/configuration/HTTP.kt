package ru.kosolapov.course.configuration

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    routing {
        swaggerUI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
    }
}
