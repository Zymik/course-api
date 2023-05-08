package ru.kosolapov.course.user


import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.kosolapov.course.user.service.UserService

fun Route.userRouting() {
    val userService by inject<UserService>()

    post<UserCredentials>("register") { user ->
        val token = userService.registerUser(user)
        call.respond(token.token)
    }

    post<UserCredentials>("login") { user ->
        val token = userService.loginUser(user)
        call.respond(token.token)
    }

}