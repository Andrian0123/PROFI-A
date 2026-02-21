package ru.profia.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Менеджер Google Play Billing для подписок.
 * Product ID создайте в Play Console → Monetization → Products → Subscriptions.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var billingClient: BillingClient? = null
    private var purchaseCallback: ((Result<Purchase>) -> Unit)? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.firstOrNull()?.let { purchase ->
                    purchaseCallback?.invoke(Result.success(purchase))
                    acknowledgePurchaseIfNeeded(purchase)
                } ?: purchaseCallback?.invoke(Result.failure(Exception("Purchase is null")))
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                purchaseCallback?.invoke(Result.failure(CancelledException()))
            }
            else -> {
                purchaseCallback?.invoke(
                    Result.failure(Exception(billingResult.debugMessage ?: "Billing error"))
                )
            }
        }
        purchaseCallback = null
    }

    init {
        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {}
            override fun onBillingServiceDisconnected() {}
        })
    }

    suspend fun isAvailable(): Boolean = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            billingClient?.let { client ->
                if (client.isReady) {
                    cont.resume(true)
                } else {
                    client.startConnection(object : BillingClientStateListener {
                        override fun onBillingSetupFinished(result: BillingResult) {
                            cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                        }
                        override fun onBillingServiceDisconnected() {
                            cont.resume(false)
                        }
                    })
                }
            } ?: cont.resume(false)
        }
    }

    fun launchPurchase(
        activity: Activity,
        productId: String,
        onResult: (Result<Purchase>) -> Unit
    ) {
        val client = billingClient
        if (client == null || !client.isReady) {
            onResult(Result.failure(Exception("Billing not ready")))
            return
        }
        GlobalScope.launch(Dispatchers.Main) {
            val productResult = queryProductDetails(productId)
            val productDetails = productResult.getOrElse { e ->
                onResult(Result.failure(e))
                return@launch
            }
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken == null) {
                onResult(Result.failure(Exception("No offer for $productId")))
                return@launch
            }
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )
                )
                .build()
            purchaseCallback = onResult
            val launchResult = client.launchBillingFlow(activity, flowParams)
            if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                purchaseCallback = null
                onResult(Result.failure(Exception(launchResult.debugMessage ?: "Launch failed")))
            }
        }
    }

    private suspend fun queryProductDetails(productId: String): Result<ProductDetails> =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                val productList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
                val params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()
                val client = billingClient
                if (client == null) {
                    cont.resume(Result.failure(Exception("BillingClient is null")))
                    return@suspendCancellableCoroutine
                }
                client.queryProductDetailsAsync(params) { result, productDetailsList ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        productDetailsList.firstOrNull()?.let {
                            cont.resume(Result.success(it))
                        } ?: cont.resume(Result.failure(Exception("Product not found: $productId")))
                    } else {
                        cont.resume(Result.failure(Exception(result.debugMessage ?: "Query failed")))
                    }
                }
            }
        }

    private fun acknowledgePurchaseIfNeeded(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            billingClient?.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            ) { }
        }
    }

    class CancelledException : Exception("Покупка отменена")
}
