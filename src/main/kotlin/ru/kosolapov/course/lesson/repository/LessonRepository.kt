package ru.kosolapov.course.lesson.repository

import ru.kosolapov.course.lesson.LessonDescription

interface LessonRepository {

    suspend fun getLessonDescription(lessonId: Long): LessonDescription?
    suspend fun getLessonDescription(courseId: Long, number: Int): LessonDescription?
    suspend fun getLessonId(courseId: Long, number: Int): Long?
    suspend fun getCourseId(lessonId: Long): Long?
    suspend fun existLesson(lessonId: Long): Boolean
    suspend fun getLessonDescriptions(courseId: Long): List<LessonDescription>
    suspend fun getLessonIds(courseId: Long): List<Long>

    suspend fun addLesson(courseId: Long, number: Int, name: String, description: String): Long
    suspend fun addLastLesson(courseId: Long, name: String, description: String): Long

    suspend fun removeLesson(lessonId: Long)
}
