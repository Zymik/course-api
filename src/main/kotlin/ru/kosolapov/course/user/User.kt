package ru.kosolapov.course.user

import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String)

@Serializable
data class UserCredentials(val name: String, val password: String)

data class UserWithPassword(val name: String, val salt: ByteArray, val password: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserWithPassword

        if (name != other.name) return false
        if (!salt.contentEquals(other.salt)) return false
        return password.contentEquals(other.password)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + password.contentHashCode()
        return result
    }
}