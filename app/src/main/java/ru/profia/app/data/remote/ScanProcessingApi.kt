package ru.profia.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import ru.profia.app.BuildConfig
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Линия «паутинки» покрытия (координаты 0..1). */
data class CoverageWebLine(
    val start: Pair<Double, Double>,
    val end: Pair<Double, Double>,
    val alpha: Double
)

/** Зона, не покрытая сканом (boundary — список [x,y] в 0..1). */
data class MissingZone(
    val boundary: List<Pair<Double, Double>>,
    val label: String
)

/** Данные покрытия от бэкенда. */
data class CoverageData(
    val percentage: Double,
    val webLines: List<CoverageWebLine>,
    val missingZones: List<MissingZone>
)

data class ScanDimensionsResult(
    val scanId: String,
    val lengthM: Double,
    val widthM: Double,
    val wallHeightM: Double,
    val perimeterM: Double,
    val floorAreaM2: Double,
    val ceilingAreaM2: Double,
    val wallAreaM2: Double,
    val coveragePercentage: Double,
    val scanQuality: Double,
    val coverageData: CoverageData? = null
)

/** Результат сканирования документа: размеры в мм и распознанное содержимое. */
data class DocumentScanResult(
    val scanId: String,
    val widthMm: Double,
    val lengthMm: Double,
    val content: List<DocumentContentLabel>,
    val hasEngineeringCommunications: Boolean
)

data class DocumentContentLabel(
    val label: String,
    val confidence: Double
)

interface ScanProcessingApi {
    suspend fun process(
        projectId: String,
        roomId: String,
        scanId: String,
        frameFiles: List<File>,
        trajectoryJson: String? = null,
        depthFiles: List<File>? = null
    ): Result<ScanDimensionsResult>

    suspend fun finish(
        projectId: String,
        roomId: String,
        scanId: String
    ): Result<ScanDimensionsResult>

    /** Сканирование документа: один файл изображения → размеры (мм) и распознанное содержимое. */
    suspend fun scanDocument(
        scanId: String,
        documentFile: File
    ): Result<DocumentScanResult>
}

@Singleton
class DefaultScanProcessingApi @Inject constructor() : ScanProcessingApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .writeTimeout(40, TimeUnit.SECONDS)
        .build()

    override suspend fun process(
        projectId: String,
        roomId: String,
        scanId: String,
        frameFiles: List<File>,
        trajectoryJson: String?,
        depthFiles: List<File>?
    ): Result<ScanDimensionsResult> = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.SCAN_SERVER_URL.trim().removeSuffix("/")
        if (baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("SCAN_SERVER_URL is empty"))
        }
        if (frameFiles.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("At least one frame is required"))
        }

        val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("project_id", projectId)
            .addFormDataPart("room_id", roomId)
            .addFormDataPart("scan_id", scanId)

        frameFiles.forEachIndexed { index, file ->
            multipart.addFormDataPart(
                "frames",
                file.name.ifBlank { "frame_${index}.jpg" },
                file.asRequestBody(JPEG_MEDIA)
            )
        }

        if (!trajectoryJson.isNullOrBlank()) {
            multipart.addFormDataPart("trajectory", trajectoryJson)
        }

        depthFiles?.forEachIndexed { index, file ->
            multipart.addFormDataPart(
                "depth",
                file.name.ifBlank { "depth_${index}.png" },
                file.asRequestBody(OCTET_MEDIA)
            )
        }

        val request = Request.Builder()
            .url("$baseUrl/api/v1/scan/process")
            .post(multipart.build())
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@runCatching Result.failure(
                        Exception("Scan process failed: ${response.code} ${response.message}")
                    )
                }
                val body = response.body?.string().orEmpty()
                Result.success(parseDimensionsResponse(body))
            }
        }.getOrElse { Result.failure(it) }
    }

    override suspend fun finish(
        projectId: String,
        roomId: String,
        scanId: String
    ): Result<ScanDimensionsResult> = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.SCAN_SERVER_URL.trim().removeSuffix("/")
        if (baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("SCAN_SERVER_URL is empty"))
        }
        val payload = JSONObject().apply {
            put("scan_id", scanId)
            put("project_id", projectId)
            put("room_id", roomId)
        }.toString()

        val request = Request.Builder()
            .url("$baseUrl/api/v1/scan/finish")
            .post(payload.toRequestBody(JSON_MEDIA))
            .addHeader("Content-Type", "application/json")
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@runCatching Result.failure(
                        Exception("Scan finish failed: ${response.code} ${response.message}")
                    )
                }
                val body = response.body?.string().orEmpty()
                Result.success(parseDimensionsResponse(body))
            }
        }.getOrElse { Result.failure(it) }
    }

    override suspend fun scanDocument(
        scanId: String,
        documentFile: File
    ): Result<DocumentScanResult> = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.SCAN_SERVER_URL.trim().removeSuffix("/")
        if (baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("SCAN_SERVER_URL is empty"))
        }
        val mime = when (documentFile.extension.lowercase()) {
            "png" -> "image/png"
            else -> "image/jpeg"
        }
        val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("scan_id", scanId)
            .addFormDataPart(
                "document",
                documentFile.name.ifBlank { "document.jpg" },
                documentFile.asRequestBody(mime.toMediaType())
            )
            .build()
        val request = Request.Builder()
            .url("$baseUrl/api/v1/scan/document")
            .post(multipart)
            .build()
        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@runCatching Result.failure(
                        Exception("Document scan failed: ${response.code} ${response.message}")
                    )
                }
                val body = response.body?.string().orEmpty()
                Result.success(parseDocumentScanResponse(body))
            }
        }.getOrElse { Result.failure(it) }
    }

    private fun parseDimensionsResponse(jsonRaw: String): ScanDimensionsResult {
        val json = JSONObject(jsonRaw.ifBlank { "{}" })
        val dimensions = json.optJSONObject("dimensions") ?: JSONObject()
        val coverage = json.optJSONObject("coverage") ?: JSONObject()
        val quality = json.optJSONObject("quality_metrics") ?: JSONObject()
        val coverageData = parseCoverageData(coverage)
        return ScanDimensionsResult(
            scanId = json.optString("scan_id", ""),
            lengthM = dimensions.optDouble("length_m", 0.0),
            widthM = dimensions.optDouble("width_m", 0.0),
            wallHeightM = dimensions.optDouble("wall_height_m", 0.0),
            perimeterM = dimensions.optDouble("perimeter_m", 0.0),
            floorAreaM2 = dimensions.optDouble("floor_area_m2", 0.0),
            ceilingAreaM2 = dimensions.optDouble("ceiling_area_m2", 0.0),
            wallAreaM2 = dimensions.optDouble("wall_area_m2", 0.0),
            coveragePercentage = coverage.optDouble("percentage", 0.0),
            scanQuality = quality.optDouble("scan_quality", 0.0),
            coverageData = coverageData
        )
    }

    private fun parseCoverageData(coverage: JSONObject): CoverageData? {
        val pct = coverage.optDouble("percentage", 0.0)
        val webLinesArray = coverage.optJSONArray("web_lines")
        val webLines = if (webLinesArray != null) (0 until webLinesArray.length()).mapNotNull { i ->
            val obj = webLinesArray.optJSONObject(i) ?: return@mapNotNull null
            val startArr = obj.optJSONArray("start")
            val endArr = obj.optJSONArray("end")
            if (startArr != null && startArr.length() >= 2 && endArr != null && endArr.length() >= 2) {
                CoverageWebLine(
                    start = startArr.optDouble(0, 0.0) to startArr.optDouble(1, 0.0),
                    end = endArr.optDouble(0, 0.0) to endArr.optDouble(1, 0.0),
                    alpha = obj.optDouble("alpha", 1.0).coerceIn(0.0, 1.0)
                )
            } else null
        } else emptyList()
        val missingArray = coverage.optJSONArray("missing_zones")
        val missingZones = if (missingArray != null) (0 until missingArray.length()).mapNotNull { i ->
            val obj = missingArray.optJSONObject(i) ?: return@mapNotNull null
            val boundaryArr = obj.optJSONArray("boundary") ?: return@mapNotNull null
            val boundary = (0 until boundaryArr.length()).mapNotNull { j ->
                val pt = boundaryArr.optJSONArray(j)
                if (pt != null && pt.length() >= 2) pt.optDouble(0, 0.0) to pt.optDouble(1, 0.0) else null
            }
            if (boundary.isNotEmpty()) MissingZone(boundary = boundary, label = obj.optString("label", "")) else null
        } else emptyList()
        return CoverageData(percentage = pct, webLines = webLines, missingZones = missingZones)
    }

    private fun parseDocumentScanResponse(jsonRaw: String): DocumentScanResult {
        val json = JSONObject(jsonRaw.ifBlank { "{}" })
        val contentArray = json.optJSONArray("content") ?: org.json.JSONArray()
        val content = (0 until contentArray.length()).map { i ->
            val obj = contentArray.optJSONObject(i) ?: JSONObject()
            DocumentContentLabel(
                label = obj.optString("label", ""),
                confidence = obj.optDouble("confidence", 0.0)
            )
        }
        return DocumentScanResult(
            scanId = json.optString("scan_id", ""),
            widthMm = json.optDouble("width_mm", 0.0),
            lengthMm = json.optDouble("length_mm", 0.0),
            content = content,
            hasEngineeringCommunications = json.optBoolean("has_engineering_communications", false)
        )
    }

    companion object {
        private val JPEG_MEDIA = "image/jpeg".toMediaType()
        private val OCTET_MEDIA = "application/octet-stream".toMediaType()
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}

