package ru.kosolapov.course.course


import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import ru.kosolapov.course.configuration.getUsername
import ru.kosolapov.course.course.service.CourseSecurityService
import ru.kosolapov.course.course.service.CourseService
import ru.kosolapov.course.course.service.checkAdmin
import ru.kosolapov.course.log
import ru.kosolapov.course.task.TaskService


@Serializable
data class CourseCreateRequest(val name: String, val description: String, val teachers: Set<String>)

@Serializable
data class AddTeachersRequest(val teachers: Set<String>)

fun Route.courseRouting() {
    val courseService by inject<CourseService>()
    val courseSecurityService by inject<CourseSecurityService>()

    with(courseSecurityService) {
        route("course") {
            post<CourseCreateRequest>("create") { course ->
                val username = getUsername()
                val id = courseService.createCourse(
                    username,
                    course.teachers,
                    course.name,
                    course.description
                )

                log.info("Created course with id: $id")
                call.respond(id)
            }

            route("{courseId}") {
                get {
                    val id = getCourseId()
                    val username = getUsername()

                    checkMember(username, id)

                    val course: Course = courseService.getCourse(id)

                    log.info("Got course with id $id")
                    call.respond(course)
                }

                post("join") {
                    val id = getCourseId()
                    val username = getUsername()
                    checkCourse(id)
                    courseService.addStudent(id, username)
                }

                get("description") {
                    val id = getCourseId()
                    checkCourse(id)
                    log.info("Got course description with id $id")
                    call.respond(courseService.getCourseDescription(id))
                }

                post<AddTeachersRequest>("teachers") {
                    val id = getCourseId()
                    val username = getUsername()
                    checkAdmin(username, id)

                    courseService.addTeachers(id, it.teachers)
                    log.info("Added teachers to course with $id")
                }
            }
        }
    }
}


fun PipelineContext<*, ApplicationCall>.getCourseId() =
    call.parameters.getOrFail<Long>("courseId")