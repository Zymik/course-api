package ru.kosolapov.course.task

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import ru.kosolapov.course.configuration.getUsername
import ru.kosolapov.course.course.getCourseId
import ru.kosolapov.course.course.service.CourseSecurityService
import ru.kosolapov.course.course.service.checkAdmin
import ru.kosolapov.course.lesson.LessonService
import ru.kosolapov.course.lesson.getLessonId
import ru.kosolapov.course.log

@Serializable
data class CreateTaskRequest(
    val name: String,
    val description: String,
    val score: Double
)

fun Route.taskRouting() {
    val taskService by inject<TaskService>()
    val courseSecurityService by inject<CourseSecurityService>()
    val lessonService by inject<LessonService>()

    with(courseSecurityService) {
        route("course/{courseId}/lesson/{lessonNumber}/task") {

            get("all") {
                val courseId = getCourseId()
                val username = getUsername()
                val lessonNumber = getLessonNumber()
                checkMember(username, courseId)
                val tasks = taskService.getTasks(courseId, lessonNumber)
                log.info("Got all tasks for course with id $courseId and lesson with number $lessonNumber")
                call.respond(tasks)
            }
        }

        route("lesson/{lessonId}/task") {
            get("all") {
                val username = getUsername()
                val lessonId = getLessonId()
                val courseId = lessonService.getCourseId(lessonId)
                checkMember(username, courseId)
                val tasks = taskService.getTasks(lessonId)
                log.info("Got all tasks for lesson with id $lessonId")
                call.respond(tasks)
            }

            post<CreateTaskRequest>("last") {
                val username = getUsername()
                val lessonId = getLessonId()
                val courseId = lessonService.getCourseId(lessonId)
                checkAdmin(username, courseId)
                val id = taskService.addLastTask(lessonId, it)
                log.info("Created task with id $id")
                call.respond(id)
            }

            post<CreateTaskRequest>("{taskNumber}") {
                val username = getUsername()
                val lessonId = getLessonId()
                val taskNumber = call.parameters.getOrFail<Int>("taskNumber")
                val courseId = lessonService.getCourseId(lessonId)
                checkAdmin(username, courseId)
                val id = taskService.addTask(lessonId, taskNumber, it)
                log.info("Created task with id $id")
                call.respond(id)
            }
        }

        route("task/{taskId}") {
            delete {
                val username = getUsername()
                val taskId = getTaskId()
                val courseId = taskService.getCourseId(taskId)
                checkAdmin(username, courseId)
                taskService.removeTask(taskId)
                log.info("Removed task with id $taskId")
            }
        }

    }

}

fun PipelineContext<*, ApplicationCall>.getLessonNumber() =
    call.parameters.getOrFail<Int>("lessonNumber")

fun PipelineContext<*, ApplicationCall>.getTaskId() =
    call.parameters.getOrFail<Long>("taskId")
