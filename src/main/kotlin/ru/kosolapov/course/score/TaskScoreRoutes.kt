package ru.kosolapov.course.score

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*
import org.koin.ktor.ext.inject
import ru.kosolapov.course.configuration.getUsername
import ru.kosolapov.course.course.service.CourseSecurityService
import ru.kosolapov.course.course.service.checkStudent
import ru.kosolapov.course.course.service.checkTeacher
import ru.kosolapov.course.task.TaskService
import ru.kosolapov.course.task.getTaskId


fun Route.taskScoreRouting() {
    val courseSecurityService by inject<CourseSecurityService>()
    val scoreService by inject<ScoreService>()
    val taskService by inject<TaskService>()

    with(courseSecurityService) {
        route("task/{taskId}/score") {

            get("user") {
                val username = getUsername()
                val taskId = getTaskId()
                val courseId = taskService.getCourseId(taskId)

                checkStudent(username, courseId)

                call.respond(scoreService.getScoreByTaskId(username, taskId))
            }

            get("{username}") {
                val username = getUsername()
                val taskId = getTaskId()
                val usernameParam = getUsernameParam()
                val courseId = taskService.getCourseId(taskId)

                checkStudent(usernameParam, courseId)
                checkMember(
                    username,
                    courseId,
                    ru.kosolapov.course.course.Role.TEACHER,
                    ru.kosolapov.course.course.Role.ADMIN
                )

                call.respond(scoreService.getScoreByTaskId(usernameParam, taskId))
            }


            get {
                val taskId = getTaskId()
                val username = getUsername()

                taskService.checkTaskExist(taskId)
                val courseId = taskService.getCourseId(taskId)

                checkMember(username, courseId)

                call.respond(scoreService.getScoreByTaskId(taskId))
            }

            post {
                val taskId = getTaskId()
                val score = call.request.queryParameters.getOrFail<Double>("score")
                val username = getUsername()
                val courseId = taskService.getCourseId(taskId)

                checkTeacher(username, courseId)

                taskService.checkTaskExist(taskId)

                scoreService.setTaskScore(username, taskId, score)
            }
        }
    }
}

fun PipelineContext<*, ApplicationCall>.getUsernameParam(): String =
    call.parameters.getOrFail("username")