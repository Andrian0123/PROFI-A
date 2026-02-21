package ru.profia.app.util

object ValidationUtils {
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
}
