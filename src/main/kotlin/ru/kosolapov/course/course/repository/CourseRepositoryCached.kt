package ru.kosolapov.course.course.repository

import ru.kosolapov.course.course.Course
import ru.kosolapov.course.course.CourseDescription
import ru.kosolapov.course.getLogger
import ru.kosolapov.course.redis.RedisCacheService

class CourseRepositoryCached(
    private val repository: CourseRepository,
    private val redisCacheService: RedisCacheService
) : CourseRepository {
    private val log = getLogger()

    companion object {
        const val COURSE_PREFIX = "course"
        const val COURSE_DESCRIPTION_PREFIX = "courseDescription"
        const val COURSE_EXIST_PREFIX = "courseExist"
    }

    override suspend fun getCourse(id: Long): Course? {
        val cachedCourse = redisCacheService.getValue<Course>("$COURSE_PREFIX:$id")
        if (cachedCourse != null) return cachedCourse
        log.info("Can't find Course with id $id in cache")

        val course = repository.getCourse(id) ?: return null

        redisCacheService.setValue("course:$id", course)
        log.info("Course with $id saved to cache")

        return course
    }

    override suspend fun getCourseDescription(id: Long): CourseDescription? {
        val cachedCourse = redisCacheService.getValue<CourseDescription>("$COURSE_DESCRIPTION_PREFIX:$id")
        if (cachedCourse != null) return cachedCourse

        log.info("Can't find CourseDescription of course with id $id in cache")

        val courseDescription = repository.getCourseDescription(id) ?: return null

        saveCourseDescription(courseDescription)
        return courseDescription
    }

    override suspend fun createCourse(name: String, description: String): Long {
        val id = repository.createCourse(name, description)
        saveCourseDescription(CourseDescription(id, name, description))
        return id
    }

    override suspend fun isCourseExist(id: Long): Boolean {
        val count = redisCacheService.exist(
            "$COURSE_PREFIX:$id",
            "$COURSE_DESCRIPTION_PREFIX:$id",
            "$COURSE_EXIST_PREFIX:$id"
        ) ?: 0

        if (count > 0) return true

        log.info("Can't find Course or CourseDescription with id $id in cache")
        val isExist = repository.isCourseExist(id)
        redisCacheService.setValue("$COURSE_EXIST_PREFIX:$id", isExist)
        log.info("Saved info about Course exist with $id to cache")

        return isExist
    }

    private suspend fun saveCourseDescription(courseDescription: CourseDescription) {
        redisCacheService.setValue("$COURSE_DESCRIPTION_PREFIX:${courseDescription.id}", courseDescription)
        log.info("Saved CourseDescription with id ${courseDescription.id} to cache")
    }
}