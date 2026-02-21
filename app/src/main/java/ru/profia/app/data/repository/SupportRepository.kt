package ru.profia.app.data.repository

import ru.profia.app.data.remote.SupportApi
import ru.profia.app.data.remote.SupportTicket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportRepository @Inject constructor(
    private val supportApi: SupportApi
) {
    suspend fun submitTicket(
        phone: String,
        email: String,
        description: String
    ): Result<Unit> = supportApi.sendTicket(
        SupportTicket(
            phone = phone,
            email = email,
            description = description
        )
    )
}
