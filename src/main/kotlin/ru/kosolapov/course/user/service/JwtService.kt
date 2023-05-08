package ru.kosolapov.course.user.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import ru.kosolapov.course.user.Token
import java.util.*
import kotlin.time.Duration

class JwtService(
    private val expireTime: Duration,
    private val algorithm: Algorithm
) {
    fun generateToken(name: String): Token = Token(
        JWT.create()
            .withClaim("name", name)
            .withExpiresAt(Date(System.currentTimeMillis() + expireTime.inWholeMilliseconds))
            .sign(algorithm)
    )
}