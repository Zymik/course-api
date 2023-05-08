package ru.kosolapov.course.user.exception

class NoSuchUserException(val name: String): Exception("No user with name: $name")