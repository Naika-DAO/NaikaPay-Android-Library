package io.naika.naikapay

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultRegistry
import io.naika.naikapay.callback.*
import io.naika.naikapay.connection.ReceiverBillingConnection
import io.naika.naikapay.exception.NaikaSignerNotFoundException

internal class BillingConnection(
    private val context: Context,
    private val walletConnectResultParser: WalletConnectResultParser,
    private val signTransactionResultParser: SignTransactionResultParser
) {

    private var callback: ConnectionCallback? = null
    private var paymentLauncher: PaymentLauncher? = null

    private var billingCommunicator: ReceiverBillingConnection? = null

    internal fun startConnection(
        networkType: NetworkType,
        connectionCallback: ConnectionCallback.() -> Unit
    ): Connection {
        callback = ConnectionCallback(disconnect = ::stopConnection).apply(connectionCallback)


        val receiverConnection = ReceiverBillingConnection()


        val canConnect = receiverConnection.startConnection(
            context,
            networkType,
            requireNotNull(callback)
        )

        if (!canConnect) {
            callback?.connectionFailed?.invoke(NaikaSignerNotFoundException())
        }

        billingCommunicator = receiverConnection
        return requireNotNull(callback)
    }

    private fun stopConnection() {

        billingCommunicator?.stopConnection()
        disconnect()

    }

    private fun disconnect() {
        callback?.disconnected?.invoke()
        callback = null
        paymentLauncher?.unregister()
        paymentLauncher = null
        //backgroundThread.dispose()
        billingCommunicator = null
    }

    fun ethCall(from: String, to: String, data: String, callback: EthCallCallback.() -> Unit) {
        billingCommunicator?.ethCall(
            from,
            to,
            data,
            callback
        )
    }

    fun getGasPrice(callback: GasPriceCallback.() -> Unit) {
        billingCommunicator?.getGasPrice(
            callback
        )
    }

    fun sendTransaction(signedTx: ByteArray, callback: SendTransactionCallback.() -> Unit) {
        billingCommunicator?.sendTransaction(
            signedTx,
            callback
        )
    }


    fun signTransaction(
        registry: ActivityResultRegistry,
        unsignedTx: ByteArray,
        selectedAccountHash: String,
        abi: String,
        callback: SignTransactionCallback.() -> Unit
    ) {
        paymentLauncher = PaymentLauncher.Builder(registry) {
            onSignTransactionActivityResult(it, callback)
        }.build()

        billingCommunicator?.signTransaction(
            requireNotNull(paymentLauncher),
            unsignedTx,
            selectedAccountHash,
            abi,
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

    }


}