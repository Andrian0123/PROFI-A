package ru.profia.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.profia.app.BuildConfig
import ru.profia.app.data.local.datastore.PreferencesDataStore
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String? = null,
    val isDemo: Boolean = false
)

interface AuthAccountApi {
    /** @param login номер телефона пользователя */
    suspend fun login(login: String, password: String): Result<AuthSession>
    /** @param login номер телефона пользователя */
    suspend fun register(login: String, password: String): Result<AuthSession>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
    suspend fun setTwoFaEnabled(enabled: Boolean): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}

@Singleton
class DefaultAuthAccountApi @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : AuthAccountApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun login(login: String, password: String): Result<AuthSession> =
        authRequest(endpoint = "/auth/login", login = login, password = password)

    override suspend fun register(login: String, password: String): Result<AuthSession> =
        authRequest(endpoint = "/auth/register", login = login, password = password)

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> =
        simpleAction("/account/change-password") {
            put("oldPassword", oldPassword)
            put("newPassword", newPassword)
        }

    override suspend fun setTwoFaEnabled(enabled: Boolean): Result<Unit> =
        simpleAction("/account/2fa") { put("enabled", enabled) }

    override suspend fun deleteAccount(): Result<Unit> =
        simpleAction("/account/delete") {}

    private suspend fun authRequest(
        endpoint: String,
        login: String,
        password: String
    ): Result<AuthSession> = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.AUTH_SERVER_URL.trim().removeSuffix("/")
        if (baseUrl.isBlank()) {
            return@withContext Result.success(
                AuthSession(
                    userId = "demo-user",
                    accessToken = "demo-token",
                    isDemo = true
                )
            )
        }

        val payload = JSONObject().apply {
            put("login", login)
            put("password", password)
        }.toString()

        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .post(payload.toRequestBody(JSON_MEDIA))
            .addHeader("Content-Type", "application/json")
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val msg = try {
                        JSONObject(body).optString("error", "Auth failed: ${response.code}")
                    } catch (_: Exception) {
                        "Auth failed: ${response.code} ${response.message}"
                    }
                    return@runCatching Result.failure(Exception(msg))
                }
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body.ifBlank { "{}" })
                val userId = json.optString("userId", "unknown")
                val accessToken = json.optString("accessToken", "")
                val refreshToken = json.optString("refreshToken", "").ifBlank { null }
                if (accessToken.isBlank()) {
                    return@runCatching Result.failure(Exception("Access token is missing"))
                }
                Result.success(AuthSession(userId, accessToken, refreshToken, isDemo = false))
            }
        }.getOrElse { Result.failure(it) }
    }

    private suspend fun simpleAction(
        endpoint: String,
        bodyBuilder: JSONObject.() -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.AUTH_SERVER_URL.trim().removeSuffix("/")
        if (baseUrl.isBlank()) {
            return@withContext Result.success(Unit)
        }
        val payload = JSONObject().apply(bodyBuilder).toString()
        val token = preferencesDataStore.getAccessToken()
        val requestBuilder = Request.Builder()
            .url("$baseUrl$endpoint")
            .post(payload.toRequestBody(JSON_MEDIA))
            .addHeader("Content-Type", "application/json")
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val request = requestBuilder.build()
        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val msg = try {
                        JSONObject(body).optString("error", "Request failed: ${response.code}")
                    } catch (_: Exception) {
                        "Request failed: ${response.code} ${response.message}"
                    }
                    return@runCatching Result.failure(Exception(msg))
                }
                Result.success(Unit)
            }
        }.getOrElse { Result.failure(it) }
    }

    companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
