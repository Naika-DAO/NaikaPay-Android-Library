package io.naika.naikapay.connection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.naika.naikapay.BuildConfig
import io.naika.naikapay.PaymentLauncher
import io.naika.naikapay.callback.ConnectWalletCallback
import io.naika.naikapay.callback.ConnectionCallback
import io.naika.naikapay.callback.SendTransactionCallback
import io.naika.naikapay.callback.SignTransactionCallback
import io.naika.naikapay.connect_wallet.WalletConnectWeakHolder
import io.naika.naikapay.constant.Const.NAIKA_SIGNER_PACKAGE_NAME
import io.naika.naikapay.constant.NaikaSignerIntent
import io.naika.naikapay.constant.NaikaSignerIntent.RESPONSE_CODE
import io.naika.naikapay.entity.SendTransactionResponse
import io.naika.naikapay.exception.DisconnectException
import io.naika.naikapay.exception.NaikaSignerNotSupportedException
import io.naika.naikapay.exception.SendTransactionException
import io.naika.naikapay.receiver.BillingReceiver
import io.naika.naikapay.receiver.BillingReceiverCommunicator
import io.naika.naikapay.security.Security
import io.naika.naikapay.sign_transaction.SignTransactionWeakHolder
import io.naika.naikapay.takeIf
import java.lang.ref.WeakReference

internal class ReceiverBillingConnection {


    private var connectionCallbackReference: WeakReference<ConnectionCallback>? = null
    private var contextReference: WeakReference<Context>? = null

    private var receiverCommunicator: BillingReceiverCommunicator? = null

    private var walletConnectWeakReference: WeakReference<WalletConnectWeakHolder>? = null
    private var signTransactionWeakReference: WeakReference<SignTransactionWeakHolder>? = null

    private var sendTxCallback: (SendTransactionCallback.() -> Unit)? = null

    private var disconnected: Boolean = false


    fun startConnection(context: Context, callback: ConnectionCallback): Boolean {
        connectionCallbackReference = WeakReference(callback)
        contextReference = WeakReference(context)


        if (!BuildConfig.DEBUG) {
            if (!Security.verifyNaikaSignerIsInstalled(context)) {
                return false
            }
        }

        createReceiverConnection()
        registerBroadcast()
        isPurchaseTypeSupported()

        return true

    }

    private fun createReceiverConnection() {
        receiverCommunicator = object : BillingReceiverCommunicator {
            override fun onNewBroadcastReceived(intent: Intent?) {
                intent?.action?.takeIf(
                    thisIsTrue = {
                        !disconnected
                    },
                    andIfNot = {
                        connectionCallbackReference?.get()?.connectionFailed?.invoke(
                            DisconnectException()
                        )
                    }
                )?.let { action ->
                    onActionReceived(action, intent.extras)
                }

            }
        }
    }

    private fun registerBroadcast() {
        BillingReceiver.addObserver(requireNotNull(receiverCommunicator))
    }

    private fun isPurchaseTypeSupported() {
        getNewIntentForBroadcast().apply {
            action = ACTION_BILLING_SUPPORT
        }.run(::sendBroadcast)
    }

    fun sendTransaction(signedTx: ByteArray, callback: SendTransactionCallback.() -> Unit) {
        sendTxCallback = callback

        getNewIntentForBroadcast().apply {
            action = ACTION_SEND_TRANSACTION
            putExtra(KEY_SIGNED_TX_BYTES_FOR_SENDING, signedTx)
        }.run(::sendBroadcast)

    }


    fun signTransaction(
        paymentLauncher: PaymentLauncher,
        unsignedTx: ByteArray,
        selectedAccountHash: String,
        callback: SignTransactionCallback.() -> Unit
    ) {
        signTransactionWeakReference = WeakReference(
            SignTransactionWeakHolder(paymentLauncher, callback)
        )

        sendSignTransactionBroadcast(unsignedTx, selectedAccountHash, callback)
    }

    private fun sendSignTransactionBroadcast(
        unsignedTx: ByteArray,
        selectedAccountHash: String,
        callback: SignTransactionCallback.() -> Unit
    ) {
        getNewIntentForBroadcast().apply {
            action = ACTION_SIGN_TRANSACTION
            putExtra(KEY_SIGN_TX_BYTES, unsignedTx)
            putExtra(KEY_SIGN_SELECTED_ADDRESS, selectedAccountHash)
        }.run(::sendBroadcast)
        Log.d("Payment", "sign tx broadcast sent")
    }

    fun connectWallet(
        paymentLauncher: PaymentLauncher,
        callback: ConnectWalletCallback.() -> Unit
    ) {
        walletConnectWeakReference = WeakReference(
            WalletConnectWeakHolder(paymentLauncher, callback)
        )

        sendConnectWalletBroadcast(callback)
    }


    private fun sendConnectWalletBroadcast(
        callback: ConnectWalletCallback.() -> Unit
    ) {
        //ConnectWalletCallback().apply(callback).purchaseFlowBegan.invoke()
        getNewIntentForBroadcast().apply {
            action = ACTION_CONNECT_WALLET
        }.run(::sendBroadcast)
    }

    private fun getNewIntentForBroadcast(): Intent {
        val bundle = Bundle().apply {
            putString(KEY_PACKAGE_NAME, contextReference?.get()?.packageName)
        }
        return Intent().apply {
            `package` = NAIKA_SIGNER_PACKAGE_NAME
            putExtras(bundle)
        }
    }

    private fun onActionReceived(action: String, extras: Bundle?) {
        when (action) {
            ACTION_RECEIVE_BILLING_SUPPORT -> {
                onBillingSupportActionReceived(extras)
            }
            ACTION_RECEIVE_WALLET_CONNECT -> {
                onWalletConnectBroadcastReceived(extras)
            }
            ACTION_RECEIVE_SIGN_TRANSACTION -> {
                onTransactionSignedBroadcastReceived(extras)
            }
            ACTION_RECEIVE_SEND_TRANSACTION -> {
                onTransactionSendBroadcastReceived(extras)
            }
        }
    }

    private fun onTransactionSendBroadcastReceived(extras: Bundle?) {
        if (sendTxCallback == null) {
            return
        }
        SendTransactionCallback().apply(requireNotNull(sendTxCallback)).run {
            if (isResponseSucceed(extras)) {
                val txHash = extras?.getString(KEY_RESPONSE_SEND_TRANSACTION_TX_HASH, "")
                sendTransactionSucceed.invoke(SendTransactionResponse(txHash!!))
            } else {
                val reasonMessage = extras?.getString(
                    KEY_RESPONSE_SEND_TRANSACTION_ERROR,
                    "Could not send transaction"
                )
                sendTransactionFailed.invoke(SendTransactionException(reasonMessage!!))
            }
        }
    }

    private fun onTransactionSignedBroadcastReceived(extras: Bundle?) {
        if (isResponseSucceed(extras)) {
            when {
                signTransactionWeakReference?.get() != null -> {
                    startSignTransactionActivity(
                        requireNotNull(signTransactionWeakReference?.get()),
                        getSignTransactionIntent(extras)
                    )
                }
                else -> {
                    // invalid state
                }
            }
        } else {
            getSignTransactionCallback()?.let { signTransactionCallback ->
                SignTransactionCallback()
                    .apply(signTransactionCallback)
                    .signTransactionFailed.invoke(DisconnectException())
            }
        }
    }

    private fun getSignTransactionIntent(extras: Bundle?): Intent? {
        return extras?.getParcelable(KEY_RESPONSE_SIGN_TRANSACTION_INTENT)
    }

    private fun onBillingSupportActionReceived(extras: Bundle?) {
        val isResponseSucceed = isResponseSucceed(extras)

        when {
            isResponseSucceed -> {
                connectionCallbackReference?.get()?.connectionSucceed?.invoke()
            }
            !isResponseSucceed -> {
                connectionCallbackReference?.get()?.connectionFailed?.invoke(
                    NaikaSignerNotSupportedException()
                )
            }
            else -> {
                connectionCallbackReference?.get()?.connectionFailed?.invoke(
                    NaikaSignerNotSupportedException()
                )
            }
        }
    }

    private fun onWalletConnectBroadcastReceived(extras: Bundle?) {
        if (isResponseSucceed(extras)) {
            when {
                walletConnectWeakReference?.get() != null -> {
                    startWalletConnectActivity(
                        requireNotNull(walletConnectWeakReference?.get()),
                        getWalletConnectIntent(extras)
                    )
                }
                else -> {
                    // invalid state
                }
            }
        } else {
            getWalletConnectCallback()?.let { walletConnectCallback ->
                ConnectWalletCallback()
                    .apply(walletConnectCallback)
                    .connectWalletFailed.invoke(DisconnectException())
            }
        }
    }

    private fun getSignTransactionCallback(): (SignTransactionCallback.() -> Unit)? {
        return when {
            signTransactionWeakReference?.get() != null -> {
                signTransactionWeakReference?.get()?.callback
            }
            else -> {
                null
            }
        }
    }

    private fun getWalletConnectCallback(): (ConnectWalletCallback.() -> Unit)? {
        return when {
            walletConnectWeakReference?.get() != null -> {
                walletConnectWeakReference?.get()?.callback
            }
            else -> {
                null
            }
        }
    }

    private fun startSignTransactionActivity(
        signTransactionWeakHolder: SignTransactionWeakHolder,
        signTxIntent: Intent?
    ) {
        signTransactionWeakHolder.paymentLauncher.activityLauncher.launch(signTxIntent)
    }

    private fun startWalletConnectActivity(
        walletConnectWeakHolder: WalletConnectWeakHolder,
        connectWalletIntent: Intent?
    ) {
        walletConnectWeakHolder.paymentLauncher.activityLauncher.launch(connectWalletIntent)
    }

    private fun getWalletConnectIntent(extras: Bundle?): Intent? {
        return extras?.getParcelable(KEY_RESPONSE_CONNECT_WALLET_INTENT)
    }

    private fun isResponseSucceed(extras: Bundle?): Boolean {
        return extras?.getInt(RESPONSE_CODE) == NaikaSignerIntent.RESPONSE_RESULT_OK
    }

    private fun sendBroadcast(intent: Intent) {
        contextReference?.get()?.sendBroadcast(intent)
    }

    fun stopConnection() {
        disconnected = true

        clearReferences()

        receiverCommunicator?.let(BillingReceiver::removeObserver)
        receiverCommunicator = null
    }

    private fun clearReferences() {
        connectionCallbackReference = null
        contextReference = null

        sendTxCallback = null

        walletConnectWeakReference?.clear()
        walletConnectWeakReference = null

        signTransactionWeakReference?.clear()
        signTransactionWeakReference = null
    }


    companion object {

        private const val ACTION_NAIKA_SIGNER_BASE = "io.naika.naikasigner."

        private const val ACTION_BILLING_SUPPORT = ACTION_NAIKA_SIGNER_BASE + "billingSupport"
        private const val ACTION_CONNECT_WALLET = ACTION_NAIKA_SIGNER_BASE + "connectWallet"
        private const val ACTION_SIGN_TRANSACTION = ACTION_NAIKA_SIGNER_BASE + "signTransaction"
        private const val ACTION_SEND_TRANSACTION = ACTION_NAIKA_SIGNER_BASE + "sendTransaction"

        private const val ACTION_RECEIVE_WALLET_CONNECT = "io.naika.naikapay.connectWallet"
        private const val ACTION_RECEIVE_BILLING_SUPPORT = "io.naika.naikapay.billingSupport"
        private const val ACTION_RECEIVE_SIGN_TRANSACTION = "io.naika.naikapay.signTransaction"
        private const val ACTION_RECEIVE_SEND_TRANSACTION = "io.naika.naikapay.sendTransaction"

        private const val KEY_PACKAGE_NAME = "packageName"
        private const val KEY_RESPONSE_CONNECT_WALLET_INTENT = "CONNECT_WALLET_INTENT"
        private const val KEY_RESPONSE_SIGN_TRANSACTION_INTENT = "SIGN_TX_INTENT"
        private const val KEY_RESPONSE_SEND_TRANSACTION_TX_HASH = "SEND_TX_HASH"
        private const val KEY_RESPONSE_SEND_TRANSACTION_ERROR = "SEND_TX_ERROR"

        private const val KEY_SIGN_TX_BYTES = "TX_BYTES"
        private const val KEY_SIGN_SELECTED_ADDRESS = "SELECTED_ADDRESS"
        private const val KEY_SIGNED_TX_BYTES_FOR_SENDING = "SEND_TX_BYTES"
    }

}