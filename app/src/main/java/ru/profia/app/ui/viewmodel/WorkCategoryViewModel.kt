package ru.profia.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.profia.app.data.model.WorkTemplate
import ru.profia.app.data.repository.PreferencesRepository
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkCategoryViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    fun addWorkTemplate(name: String, category: String, unitAbbr: String, defaultPrice: Double) {
        viewModelScope.launch {
            val template = WorkTemplate(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                category = category,
                unitAbbr = unitAbbr,
                defaultPrice = defaultPrice.coerceAtLeast(0.0)
            )
            preferencesRepository.addWorkTemplate(template)
        }
    }
}
