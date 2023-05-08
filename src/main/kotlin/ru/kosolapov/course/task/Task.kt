package ru.kosolapov.course.task

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Long,
    val lessonId: Long,
    val number: Int,
    val name: String,
    val description: String,
    val score: Double
)
