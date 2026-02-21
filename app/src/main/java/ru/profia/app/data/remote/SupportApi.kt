package ru.profia.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.profia.app.BuildConfig
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class SupportTicket(
    val phone: String,
    val email: String,
    val description: String
)

interface SupportApi {
    suspend fun sendTicket(ticket: SupportTicket): Result<Unit>
}

@Singleton
class DefaultSupportApi @Inject constructor() : SupportApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun sendTicket(ticket: SupportTicket): Result<Unit> = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.SUPPORT_SERVER_URL.trim().removeSuffix("/")
        if (baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("SUPPORT_SERVER_URL is empty"))
        }

        val payload = JSONObject().apply {
            put("phone", ticket.phone)
            put("email", ticket.email)
            put("description", ticket.description)
        }.toString()

        val request = Request.Builder()
            .url("$baseUrl/support/tickets")
            .post(payload.toRequestBody(JSON_MEDIA))
            .addHeader("Content-Type", "application/json")
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@runCatching Result.failure(
                        Exception("Support request failed: ${response.code} ${response.message}")
                    )
                }
                Result.success(Unit)
            }
        }.getOrElse { Result.failure(it) }
    }

    companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
