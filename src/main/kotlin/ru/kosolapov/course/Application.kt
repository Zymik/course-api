package ru.kosolapov.course

import CourseMemberRepositoryDatabase
import CourseRepositoryDatabase
import com.auth0.jwt.algorithms.Algorithm
import configureDatabase
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import org.koin.ktor.plugin.koin
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger
import ru.kosolapov.course.configuration.*
import ru.kosolapov.course.course.repository.CourseMemberRepository
import ru.kosolapov.course.course.repository.CourseRepository
import ru.kosolapov.course.course.repository.CourseRepositoryCached
import ru.kosolapov.course.course.service.CourseSecurityService
import ru.kosolapov.course.course.service.CourseService
import ru.kosolapov.course.lesson.LessonService
import ru.kosolapov.course.lesson.repository.LessonRepository
import ru.kosolapov.course.lesson.repository.LessonRepositoryDatabase
import ru.kosolapov.course.redis.RedisCacheService
import ru.kosolapov.course.redis.RedisConnectionService
import ru.kosolapov.course.score.ScoreService
import ru.kosolapov.course.score.UserTaskScoreRepository
import ru.kosolapov.course.score.UserTaskScoreRepositoryDatabase
import ru.kosolapov.course.task.TaskService
import ru.kosolapov.course.task.repository.TaskRepository
import ru.kosolapov.course.task.repository.TaskRepositoryDatabase
import ru.kosolapov.course.user.UserRepository
import ru.kosolapov.course.user.UserRepositoryDatabase
import ru.kosolapov.course.user.service.JwtService
import ru.kosolapov.course.user.service.PasswordHashService
import ru.kosolapov.course.user.service.UserService
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


fun main(args: Array<String>) = EngineMain.main(args)


fun Application.module() {

    val algorithm: Algorithm = Algorithm.HMAC256(environment.config.property("jwt.secret").getString())
    val duration: Duration = environment.config.property("jwt.expire_time_seconds")
        .getString()
        .toLong()
        .toDuration(DurationUnit.SECONDS)

    val redisPool = configureRedisPool()
    configureDatabase()

    koin {
        modules(
            org.koin.dsl.module {
                single { RedisConnectionService(redisPool) }
                single {
                    RedisCacheService(
                        get(),
                        environment.config.property("redis.expire_seconds").getString().toLong()
                    )
                }
                single<LessonRepository> { LessonRepositoryDatabase() }
                single<CourseRepositoryDatabase> { CourseRepositoryDatabase(get()) }
                single<CourseRepository> { CourseRepositoryCached(get<CourseRepositoryDatabase>(), get()) }
                single<TaskRepository> { TaskRepositoryDatabase() }
                single<CourseMemberRepository> { CourseMemberRepositoryDatabase() }
                single<UserRepository> { UserRepositoryDatabase() }
                single<UserTaskScoreRepository> { UserTaskScoreRepositoryDatabase() }
                single { algorithm }
                single { duration }
                single<CourseSecurityService> { CourseSecurityService(get(), get()) }
                single { PasswordHashService() }
                single { JwtService(get(), get()) }
                single { UserService(get(), get(), get()) }
                single { TaskService(get(), get()) }
                single { LessonService(get()) }
                single { ScoreService(get(), get(), get()) }
                single { CourseService(get(), get()) }
            }
        )
    }

    configureSerialization()
    configureSecurity(algorithm)
    configureRouting()
    configureHTTP()
    configureStatusPage()

}

inline val PipelineContext<*, ApplicationCall>.log get() = call.application.log


inline fun <reified T> T.getLogger(): Logger =
    getLogger(T::class.java)