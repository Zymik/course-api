package ru.kosolapov.course.lesson

import ru.kosolapov.course.lesson.repository.LessonRepository


class LessonService(
    private val lessonRepository: LessonRepository
) {
    suspend fun addLesson(courseId: Long, number: Int, lesson: CreateLessonRequest): Long =
        lessonRepository.addLesson(courseId, number, lesson.name, lesson.description)

    suspend fun getLessonDescriptions(courseId: Long): List<LessonDescription> =
        lessonRepository.getLessonDescriptions(courseId)

    suspend fun removeLesson(lessonId: Long) = lessonRepository.removeLesson(lessonId)

    suspend fun getLessonDescription(courseId: Long, number: Int): LessonDescription =
        lessonRepository.getLessonDescription(courseId, number) ?: throw NoSuchLessonException(courseId, number)

    suspend fun addLastLesson(courseId: Long, lesson: CreateLessonRequest): Long =
        lessonRepository.addLastLesson(courseId, lesson.name, lesson.description)

    suspend fun getLessonDescription(lessonId: Long): LessonDescription =
        lessonRepository.getLessonDescription(lessonId) ?: throw NoSuchLessonException(lessonId)

    suspend fun getLessonId(courseId: Long, number: Int): Long =
        lessonRepository.getLessonId(courseId, number) ?: throw NoSuchLessonException(courseId, number)

    suspend fun getCourseId(lessonId: Long): Long =
        lessonRepository.getCourseId(lessonId) ?: throw NoSuchLessonException(lessonId)
}

