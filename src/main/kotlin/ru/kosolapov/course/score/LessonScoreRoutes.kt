package ru.kosolapov.course.score

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import ru.kosolapov.course.configuration.getUsername
import ru.kosolapov.course.course.service.CourseSecurityService
import ru.kosolapov.course.course.service.checkStudent
import ru.kosolapov.course.lesson.LessonService
import ru.kosolapov.course.lesson.getLessonId


fun Route.lessonScoreRouting() {
    val courseSecurityService by inject<CourseSecurityService>()
    val scoreService by inject<ScoreService>()
    val lessonService by inject<LessonService>()

    with(courseSecurityService) {
        get("lesson/{lessonId}/score") {
            val username = getUsername()
            val taskId = getLessonId()
            val courseId = lessonService.getCourseId(taskId)

            checkStudent(username, courseId)

            call.respond(scoreService.getScoreByLessonId(taskId))
        }

        get("lesson/{lessonId}/score/user") {
            val username = getUsername()
            val lessonId = getLessonId()
            val courseId = lessonService.getCourseId(lessonId)

            checkStudent(username, courseId)

            call.respond(scoreService.getScoreByLessonId(username, lessonId))
        }

        get("lesson/{lessonId}/score/user/{username}") {
            val username = getUsername()
            val lessonId = getLessonId()
            val usernameParam = getUsernameParam()
            val courseId = lessonService.getCourseId(lessonId)

            checkStudent(usernameParam, courseId)
            checkMember(username, courseId, ru.kosolapov.course.course.Role.TEACHER, ru.kosolapov.course.course.Role.ADMIN)

            call.respond(scoreService.getScoreByLessonId(usernameParam, lessonId))
        }
    }
}