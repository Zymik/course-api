package ru.kosolapov.course.score

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.kosolapov.course.configuration.getUsername
import ru.kosolapov.course.course.getCourseId
import ru.kosolapov.course.course.service.CourseSecurityService
import ru.kosolapov.course.course.service.checkStudent

fun Route.courseScoreRouting() {
    val courseSecurityService by inject<CourseSecurityService>()
    val scoreService by inject<ScoreService>()

    with(courseSecurityService) {
        get("course/{courseId}/score") {
            val username = getUsername()
            val courseId = getCourseId()

            checkStudent(username, courseId)

            call.respond(scoreService.getScoreByCourseId(courseId))
        }

        get("course/{courseId}/score/user") {
            val username = getUsername()
            val courseId = getCourseId()

            checkStudent(username, courseId)

            call.respond(scoreService.getScoreByCourseId(username, courseId))
        }

        get("course/{courseId}/score/user/{username}") {
            val username = getUsername()
            val usernameParam = getUsernameParam()
            val courseId = getCourseId()

            checkStudent(usernameParam, courseId)
            checkMember(username, courseId, ru.kosolapov.course.course.Role.TEACHER, ru.kosolapov.course.course.Role.ADMIN)

            call.respond(scoreService.getScoreByCourseId(usernameParam, courseId))
        }
    }
}