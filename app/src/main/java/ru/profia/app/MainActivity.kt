package ru.profia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import ru.profia.app.data.repository.PreferencesRepository
import ru.profia.app.ui.navigation.ProfiANavHost
import ru.profia.app.ui.theme.ProfiATheme
import ru.profia.app.ui.util.AppLocaleHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            runBlocking {
                val language = preferencesRepository.appSettings.first().language
                AppLocaleHelper.applyLanguage(language)
            }
        } catch (e: Exception) {
            AppLocaleHelper.applyLanguage("RU")
        }
        ComposeFoundationFlags.isNonComposedClickableEnabled = false
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ProfiATheme {
                ru.profia.app.ui.util.ProvideWindowFormFactor {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        ProfiANavHost()
                    }
                }
            }
        }
    }
}
