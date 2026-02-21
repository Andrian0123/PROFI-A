package ru.profia.app.data.remote

import com.android.billingclient.api.Purchase
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

/**
 * Результат верификации покупки на сервере.
 * @param valid — покупка подтверждена сервером
 * @param endDateMillis — дата окончания подписки (если сервер вернул), иначе null
 */
data class VerificationResult(
    val valid: Boolean,
    val endDateMillis: Long? = null
)

/**
 * Верификация покупок Google Play на собственном сервере.
 * Если [BuildConfig.VERIFICATION_SERVER_URL] пустой — верификация пропускается (активация только по клиенту).
 * Контракт сервера см. в README, раздел "Верификация покупок на сервере".
 */
interface PurchaseVerificationApi {
    suspend fun verify(purchase: Purchase): Result<VerificationResult>
}

@Singleton
class DefaultPurchaseVerificationApi @Inject constructor() : PurchaseVerificationApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun verify(purchase: Purchase): Result<VerificationResult> = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.VERIFICATION_SERVER_URL?.trim()?.removeSuffix("/")
        if (baseUrl.isNullOrEmpty()) {
            return@withContext Result.success(VerificationResult(valid = true, endDateMillis = null))
        }
        val productId = purchase.products.firstOrNull() ?: return@withContext Result.failure(
            IllegalArgumentException("Purchase has no product id")
        )
        val body = JSONObject().apply {
            put("purchaseToken", purchase.purchaseToken)
            put("productId", productId)
            put("orderId", purchase.orderId ?: "")
        }.toString()
        val request = Request.Builder()
            .url("$baseUrl/verify")
            .post(body.toRequestBody(JSON_MEDIA))
            .addHeader("Content-Type", "application/json")
            .build()
        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@runCatching Result.failure(
                        Exception("Server error: ${response.code} ${response.message}")
                    )
                }
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                val valid = json.optBoolean("valid", false)
                val endDate = if (json.has("endDateMillis")) json.getLong("endDateMillis") else null
                Result.success(VerificationResult(valid = valid, endDateMillis = endDate))
            }
        }.getOrElse { Result.failure(it) }
    }

    companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
