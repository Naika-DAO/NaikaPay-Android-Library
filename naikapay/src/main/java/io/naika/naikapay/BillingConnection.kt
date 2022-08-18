package io.naika.naikapay

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultRegistry
import io.naika.naikapay.callback.ConnectWalletCallback
import io.naika.naikapay.callback.ConnectionCallback
import io.naika.naikapay.connection.ReceiverBillingConnection

internal class BillingConnection(
    private val context: Context,
    private val walletConnectResultParser: WalletConnectResultParser
) {

    private var callback: ConnectionCallback? = null
    private var paymentLauncher: PaymentLauncher? = null

    private var billingCommunicator: ReceiverBillingConnection? = null

    internal fun startConnection(connectionCallback: ConnectionCallback.() -> Unit): Connection {
        callback = ConnectionCallback(disconnect = ::stopConnection).apply(connectionCallback)


        val receiverConnection = ReceiverBillingConnection()


        receiverConnection.startConnection(
            context,
            requireNotNull(callback)
        )

        billingCommunicator = receiverConnection
        return requireNotNull(callback)
    }

    private fun stopConnection() {
//        runOnCommunicator(TAG_STOP_CONNECTION) { billingCommunicator ->
//            billingCommunicator.stopConnection()
//            disconnect()
//        }
    }


    fun connectWallet(
        registry: ActivityResultRegistry,
        callback: ConnectWalletCallback.() -> Unit
    ) {
        paymentLauncher = PaymentLauncher.Builder(registry) {
            onActivityResult(it, callback)
        }.build()

        billingCommunicator?.connectWallet(
            requireNotNull(paymentLauncher),
            callback
        )

    }

    private fun onActivityResult(
        activityResult: ActivityResult,
        connectWalletCallback: ConnectWalletCallback.() -> Unit
    ) {
        when (activityResult.resultCode) {
            Activity.RESULT_OK -> {
                walletConnectResultParser.handleReceivedResult(
                    activityResult.data,
                    connectWalletCallback
                )
            }
            Activity.RESULT_CANCELED -> {
                ConnectWalletCallback().apply(connectWalletCallback)
                    .connectWalletCanceled
                    .invoke()
            }
            else -> {
                ConnectWalletCallback().apply(connectWalletCallback)
                    .connectWalletFailed
                    .invoke(IllegalStateException("Result code is not valid"))
            }
        }
    }


    companion object {

        const val PAYMENT_SERVICE_KEY = "payment_service_key"

        private const val TAG_STOP_CONNECTION = "stopConnection"
        private const val TAG_QUERY_PURCHASE_PRODUCT = "queryPurchasedProducts"
        private const val TAG_CONSUME = "consume"
        private const val TAG_PURCHASE = "purchase"
        private const val TAG_GET_SKU_DETAIL = "skuDetial"
        private const val TAG_CHECK_TRIAL_SUBSCRIPTION = "checkTrialSubscription"
    }


}