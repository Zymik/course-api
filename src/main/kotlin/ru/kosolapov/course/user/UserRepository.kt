package ru.kosolapov.course.user

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import ru.kosolapov.course.table.UserTable
import ru.kosolapov.course.user.service.HashedPassword


interface UserRepository {
    suspend fun get(name: String): User?
    suspend fun getWithPassword(name: String): UserWithPassword?
    suspend fun save(name: String, hashedPassword: HashedPassword)
    suspend fun exist(name: String): Boolean
}

class UserRepositoryDatabase : UserRepository {

    override suspend fun get(name: String): User? =
        newSuspendedTransaction(Dispatchers.IO) {
            UserTable.select { UserTable.name eq name }
                .map { User(it[UserTable.name]) }
                .singleOrNull()
        }


    override suspend fun getWithPassword(name: String): UserWithPassword? =
        newSuspendedTransaction(Dispatchers.IO) {
            UserTable.select { UserTable.name eq name }
                .map { UserWithPassword(it[UserTable.name], it[UserTable.salt], it[UserTable.password]) }
                .singleOrNull()
        }

    override suspend fun save(name: String, hashedPassword: HashedPassword) =
        newSuspendedTransaction(Dispatchers.IO) {
            UserTable.insert {
                it[UserTable.name] = name
                it[salt] = hashedPassword.salt
                it[password] = hashedPassword.hash
            }
            return@newSuspendedTransaction
        }

    override suspend fun exist(name: String): Boolean =
        newSuspendedTransaction(Dispatchers.IO) {
            UserTable.select { UserTable.name eq name}
                .count() > 0
        }

}
