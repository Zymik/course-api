package ru.kosolapov.course.lesson

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
import ru.kosolapov.course.log

@Serializable
data class CreateLessonRequest(val name: String, val description: String)


fun Route.lessonRouting() {
    val lessonService by inject<LessonService>()
    val courseSecurityService by inject<CourseSecurityService>()

    with(courseSecurityService) {
        route("course/{courseId}/lesson") {
            get("all") {
                val courseId = getCourseId()
                val username = getUsername()
                checkMember(username, courseId)
                val lessons = lessonService.getLessonDescriptions(courseId)
                log.info("Got lessons for course with id $courseId")

                call.respond(lessons)
            }

            route("{number}") {
                fun PipelineContext<*, ApplicationCall>.getNumber() =
                    call.parameters.getOrFail<Int>("number")

                post<CreateLessonRequest> {
                    val courseId = getCourseId()
                    val number = getNumber()
                    val username = getUsername()
                    checkAdmin(username, courseId)
                    val id = lessonService.addLesson(courseId, number, it)
                    log.info("Created lesson with id $id")

                    call.respond(id)
                }

                get {
                    val courseId = getCourseId()
                    val number = getNumber()
                    val username = getUsername()
                    checkMember(username, courseId)

                    val lesson = lessonService.getLessonId(courseId, number)

                    log.info("Got lesson with courseId $courseId and number $number")

                    call.respond(lesson)
                }
            }

            post<CreateLessonRequest>("last") {
                val courseId = getCourseId()
                val username = getUsername()
                checkAdmin(username, courseId)
                val id = lessonService.addLastLesson(courseId, it)
                log.info("Created lesson with id $id")

                call.respond(id)
            }
        }

        route("lesson/{lessonId}") {
            get {
                val lessonId = getLessonId()
                val username = getUsername()
                val courseId = lessonService.getCourseId(lessonId)
                checkMember(username, courseId)
                val lesson = lessonService.getLessonDescription(lessonId)
                log.info("Got lesson description with id $lessonId")
                call.respond(lesson)
            }
            delete {
                val lessonId = getLessonId()
                val username = getUsername()
                val courseId = lessonService.getCourseId(lessonId)
                checkAdmin(username, courseId)
                lessonService.removeLesson(lessonId)
                log.info("Removed lesson with id $lessonId")
            }
        }
    }
}


fun PipelineContext<*, ApplicationCall>.getLessonId() =
    call.parameters.getOrFail<Long>("lessonId")