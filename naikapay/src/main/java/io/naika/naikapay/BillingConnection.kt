package io.naika.naikapay

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultRegistry
import io.naika.naikapay.callback.ConnectWalletCallback
import io.naika.naikapay.callback.ConnectionCallback
import io.naika.naikapay.callback.SignTransactionCallback
import io.naika.naikapay.connection.ReceiverBillingConnection

internal class BillingConnection(
    private val context: Context,
    private val walletConnectResultParser: WalletConnectResultParser,
    private val signTransactionResultParser: SignTransactionResultParser
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


    fun signTransaction(
        registry: ActivityResultRegistry,
        unsignedTx: ByteArray,
        selectedAccountHash: String,
        callback: SignTransactionCallback.() -> Unit
    ) {
        paymentLauncher = PaymentLauncher.Builder(registry) {
            onSignTransactionActivityResult(it, callback)
        }.build()

        billingCommunicator?.signTransaction(
            requireNotNull(paymentLauncher),
            unsignedTx,
            selectedAccountHash,
            callback
        )
    }

    private fun onSignTransactionActivityResult(
        activityResult: ActivityResult,
        callback: SignTransactionCallback.() -> Unit
    ) {
        when (activityResult.resultCode) {
            Activity.RESULT_OK -> {
                signTransactionResultParser.handleReceivedResult(
                    activityResult.data,
                    callback
                )
            }
            Activity.RESULT_CANCELED -> {
                SignTransactionCallback().apply(callback)
                    .signTransactionCanceled
                    .invoke()
            }
            else -> {
                SignTransactionCallback().apply(callback)
                    .signTransactionFailed
                    .invoke(IllegalStateException("Result code is not valid"))
            }
        }
    }


    fun connectWallet(
        registry: ActivityResultRegistry,
        callback: ConnectWalletCallback.() -> Unit
    ) {
        paymentLauncher = PaymentLauncher.Builder(registry) {
            onConnectWalletActivityResult(it, callback)
        }.build()

        billingCommunicator?.connectWallet(
            requireNotNull(paymentLauncher),
            callback
        )

    }

    private fun onConnectWalletActivityResult(
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