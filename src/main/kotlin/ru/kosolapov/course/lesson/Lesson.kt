package ru.kosolapov.course.lesson

import kotlinx.serialization.Serializable
import ru.kosolapov.course.task.Task

@Serializable
data class Lesson(
    val id: Long,
    val courseId: Long,
    val number: Int,
    val name: String,
    val description: String,
    val tasks: List<Task>,
)

@Serializable
data class LessonDescription(
    val id: Long,
    val courseId: Long,
    val number: Int,
    val name: String,
    val description: String
)