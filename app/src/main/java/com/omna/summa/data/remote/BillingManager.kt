package com.omna.summa.data.remote

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

sealed class BillingEvent{
    data class PurchaseConfirmed(val purchase: Purchase) : BillingEvent()
    data class PurchaseError(val message: String) : BillingEvent()
}

class BillingManager(context: Context, private val onEvent: (BillingEvent) -> Unit) {
    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if(result.responseCode == BillingClient.BillingResponseCode.OK){
           purchases?.forEach { handlePurchase(it) }
        }else{
            onEvent(BillingEvent.PurchaseError(result.debugMessage))
        }
    }

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    fun startConnection(){
        billingClient.startConnection(object : BillingClientStateListener{
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if(billingResult.responseCode == BillingClient.BillingResponseCode.OK){
                    queryActivePurchases()
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    fun queryActivePurchases(){
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params){ result, purchases ->
            if(result.responseCode == BillingClient.BillingResponseCode.OK){
                purchases.forEach{handlePurchase(it)}
            }
        }
    }

    private fun handlePurchase(purchase: Purchase){
        if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED){
            if(!purchase.isAcknowledged){
                acknowledgePurchase(purchase)
            }

            onEvent(BillingEvent.PurchaseConfirmed(purchase))
        }
    }

    private fun acknowledgePurchase(purchase: Purchase){
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params){}
    }

    fun launchProSubscription(activity: Activity){
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("summa_pro")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(params) {result, productDetailsList ->
            if(result.responseCode != BillingClient.BillingResponseCode.OK){
                onEvent(BillingEvent.PurchaseError("Erro ao buscar produto"))
                return@queryProductDetailsAsync
            }

            if(productDetailsList.isEmpty()){
                onEvent(BillingEvent.PurchaseError("Produto não encontrado"))
                return@queryProductDetailsAsync
            }

            val productDetails = productDetailsList.first()

            launchBillingFlow(activity, productDetails)
        }
    }

    private fun launchBillingFlow(activity: Activity, productDetails: ProductDetails){
        val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()

        if(offerDetails == null){
            onEvent(BillingEvent.PurchaseError("Nenhuma oferta disponível"))
            return
        }

        val billingParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerDetails.offerToken)
                        .build()
                )
            ).build()

        billingClient.launchBillingFlow(activity, billingParams)
    }
}