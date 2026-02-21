package ru.profia.app.data.model

data class UserProfile(
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    val email: String,
    val phone: String,
    val companyName: String? = null,
    val inn: String? = null,
    val kpp: String? = null,
    val legalAddress: String? = null,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val correspondentAccount: String? = null,
    val bic: String? = null
)

data class AppSettings(
    val city: String = "Краснодар",
    val currency: String = "RUR",
    val language: String = "RU",
    val unitSystem: String = "Метры"
)
