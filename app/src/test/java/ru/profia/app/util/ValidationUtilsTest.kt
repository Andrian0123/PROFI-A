package ru.profia.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun isValidEmail_validEmails_returnsTrue() {
        assertEquals(true, ValidationUtils.isValidEmail("user@example.com"))
        assertEquals(true, ValidationUtils.isValidEmail("test+tag@gmail.com"))
        assertEquals(true, ValidationUtils.isValidEmail("name.surname@company.ru"))
        assertEquals(true, ValidationUtils.isValidEmail("a@b.co"))
    }

    @Test
    fun isValidEmail_invalidEmails_returnsFalse() {
        assertEquals(false, ValidationUtils.isValidEmail(""))
        assertEquals(false, ValidationUtils.isValidEmail("   "))
        assertEquals(false, ValidationUtils.isValidEmail("invalid"))
        assertEquals(false, ValidationUtils.isValidEmail("@domain.com"))
        assertEquals(false, ValidationUtils.isValidEmail("user@"))
        assertEquals(false, ValidationUtils.isValidEmail("user@.com"))
    }

    @Test
    fun isValidEmail_withSpaces_trimmedAndChecked() {
        assertEquals(true, ValidationUtils.isValidEmail("  user@example.com  "))
    }
}
