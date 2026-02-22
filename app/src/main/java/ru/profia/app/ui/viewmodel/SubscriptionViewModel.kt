package ru.profia.app.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.profia.app.billing.BillingManager
import ru.profia.app.billing.BillingProducts
import ru.profia.app.data.model.SubscriptionData
import ru.profia.app.data.repository.SubscriptionRepository
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    val subscription: StateFlow<SubscriptionData> = subscriptionRepository.subscription
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createEmptySubscription())

    val isReadOnly: StateFlow<Boolean> = subscriptionRepository.isReadOnly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val trialDaysRemaining: StateFlow<Int?> = subscriptionRepository.trialDaysRemaining
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Доступ к сканеру помещений и сканеру документов (зависит от SCANNER_REQUIRES_SUBSCRIPTION в репозитории). */
    val hasScannerAccess: StateFlow<Boolean> = subscriptionRepository.hasScannerAccess
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** Режим подписки и авторизация обязательны с 01.06.2026. До этой даты — полный доступ без входа. */
    fun isSubscriptionRequired(): Boolean = subscriptionRepository.isSubscriptionRequiredNow()

    fun startTrial() {
        viewModelScope.launch {
            subscriptionRepository.startTrial()
        }
    }

    fun activatePromoCode(code: String, onResult: (Result<Int>) -> Unit) {
        viewModelScope.launch {
            onResult(subscriptionRepository.activatePromoCode(code))
        }
    }

    fun purchaseSubscription(durationMonths: Int) {
        viewModelScope.launch {
            subscriptionRepository.purchaseSubscription(durationMonths)
        }
    }

    fun startPurchase(
        activity: Activity,
        durationMonths: Int,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val available = billingManager.isAvailable()
            if (!available) {
                subscriptionRepository.purchaseSubscription(durationMonths)
                onResult(Result.success(Unit))
                return@launch
            }
            val productId = BillingProducts.productIdForMonths(durationMonths)
            billingManager.launchPurchase(activity, productId) { purchaseResult ->
                purchaseResult.fold(
                    onSuccess = { purchase ->
                        viewModelScope.launch {
                            val activateResult = subscriptionRepository.activatePurchase(purchase, durationMonths)
                            onResult(activateResult)
                        }
                    },
                    onFailure = { onResult(Result.failure(it)) }
                )
            }
        }
    }

    private fun createEmptySubscription() = SubscriptionData(
        type = ru.profia.app.data.model.SubscriptionType.NONE,
        startDate = 0L,
        endDate = 0L,
        isActive = false
    )
}
