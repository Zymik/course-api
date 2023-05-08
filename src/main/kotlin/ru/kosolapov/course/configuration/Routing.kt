package ru.kosolapov.course.configuration

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import ru.kosolapov.course.course.courseRouting
import ru.kosolapov.course.lesson.lessonRouting
import ru.kosolapov.course.score.courseScoreRouting
import ru.kosolapov.course.score.lessonScoreRouting
import ru.kosolapov.course.score.taskScoreRouting
import ru.kosolapov.course.task.taskRouting
import ru.kosolapov.course.user.userRouting

fun Application.configureRouting() {
    routing {
        route("v1") {
            userRouting()
            authenticate("auth-jwt") {
                courseRouting()
                lessonRouting()
                taskRouting()
                taskScoreRouting()
                lessonScoreRouting()
                courseScoreRouting()
            }
        }
    }
}

fun PipelineContext<*, ApplicationCall>.getUsername(): String =
    call.principal<JWTPrincipal>()!!.payload.getClaim("name").asString()

