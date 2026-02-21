package ru.profia.app.data.repository

import ru.profia.app.data.remote.ScanDimensionsResult
import ru.profia.app.data.remote.ScanProcessingApi
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanProcessingRepository @Inject constructor(
    private val scanProcessingApi: ScanProcessingApi
) {
    suspend fun processScan(
        projectId: String,
        roomId: String,
        scanId: String,
        frameFiles: List<File>,
        trajectoryJson: String? = null,
        depthFiles: List<File>? = null
    ): Result<ScanDimensionsResult> =
        scanProcessingApi.process(projectId, roomId, scanId, frameFiles, trajectoryJson, depthFiles)

    suspend fun finishScan(
        projectId: String,
        roomId: String,
        scanId: String
    ): Result<ScanDimensionsResult> =
        scanProcessingApi.finish(projectId, roomId, scanId)
}

