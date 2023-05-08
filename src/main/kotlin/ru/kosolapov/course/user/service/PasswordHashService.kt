package ru.kosolapov.course.user.service

import java.security.MessageDigest
import kotlin.random.Random

data class HashedPassword(val hash: ByteArray, val salt: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HashedPassword

        if (!hash.contentEquals(other.hash)) return false
        return salt.contentEquals(other.salt)
    }

    override fun hashCode(): Int {
        var result = hash.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        return result
    }
}

class PasswordHashService {
    companion object {
        const val SALT_SIZE = 16
        const val ALGORITHM = "SHA-256"
    }

    private val digest = MessageDigest.getInstance(ALGORITHM)

    fun hash(password: String, salt: ByteArray): ByteArray {
        return digest.digest(salt + password.toByteArray())
    }

    fun hashPassword(password: String): HashedPassword {
        val salt = Random.nextBytes(SALT_SIZE)
        val hash = hash(password, salt)
        return HashedPassword(hash, salt)
    }

}