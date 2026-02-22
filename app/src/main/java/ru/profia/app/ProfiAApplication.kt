package ru.profia.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

@HiltAndroidApp
class ProfiAApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        appScope.launch(Dispatchers.IO) { cleanupAppCache() }
    }

    /** Очистка устаревших файлов в кэше для экономии места (сканы, экспорты). */
    private fun cleanupAppCache() {
        val cacheDir = cacheDir ?: return
        val now = System.currentTimeMillis()
        val maxAgeMs = 2 * 24 * 60 * 60 * 1000L // 2 суток

        listOf("document_scan", "scan_frames").forEach { subDirName ->
            val dir = File(cacheDir, subDirName)
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { f ->
                    if (f.isFile && now - f.lastModified() > maxAgeMs) {
                        try { f.delete() } catch (_: Exception) { }
                    }
                }
            }
        }
        // Старые экспортные PDF/CSV в корне cacheDir (estimate_*, akt_*, KS-2_*, KS-3_*)
        cacheDir.listFiles()?.forEach { f ->
            if (f.isFile && (f.name.startsWith("estimate_") || f.name.startsWith("akt_") || f.name.startsWith("KS-2_") || f.name.startsWith("KS-3_")) && now - f.lastModified() > maxAgeMs) {
                try { f.delete() } catch (_: Exception) { }
            }
        }
    }
}
