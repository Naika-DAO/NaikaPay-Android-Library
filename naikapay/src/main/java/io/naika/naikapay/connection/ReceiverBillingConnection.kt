package io.naika.naikapay.connection

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.naika.naikapay.PaymentLauncher
import io.naika.naikapay.callback.ConnectWalletCallback
import io.naika.naikapay.callback.ConnectionCallback
import io.naika.naikapay.connect_wallet.WalletConnectWeakHolder
import io.naika.naikapay.constant.Const.NAIKA_SIGNER_PACKAGE_NAME
import io.naika.naikapay.constant.NaikaSignerIntent
import io.naika.naikapay.constant.NaikaSignerIntent.RESPONSE_CODE
import io.naika.naikapay.receiver.BillingReceiver
import io.naika.naikapay.receiver.BillingReceiverCommunicator
import java.lang.ref.WeakReference

internal class ReceiverBillingConnection {


    private var connectionCallbackReference: WeakReference<ConnectionCallback>? = null
    private var contextReference: WeakReference<Context>? = null

    private var receiverCommunicator: BillingReceiverCommunicator? = null

    private var walletConnectWeakReference: WeakReference<WalletConnectWeakHolder>? = null


    fun startConnection(context: Context, callback: ConnectionCallback): Boolean {
        connectionCallbackReference = WeakReference(callback)
        contextReference = WeakReference(context)



        return when {
            canConnectWithReceiverComponent() -> {
                createReceiverConnection()
                registerBroadcast()
                isPurchaseTypeSupported()
                true
            }
            else -> {
                false
            }
        }
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

    private fun canConnectWithReceiverComponent(): Boolean {
        return true
    }

    private fun registerBroadcast() {
        BillingReceiver.addObserver(requireNotNull(receiverCommunicator))
    }

    private fun isPurchaseTypeSupported() {
        getNewIntentForBroadcast().apply {
            action = ACTION_BILLING_SUPPORT
        }.run(::sendBroadcast)
    }

    private fun getNewIntentForBroadcast(): Intent {
        val bundle = Bundle().apply {
            putString(KEY_PACKAGE_NAME, contextReference?.get()?.packageName)
            //putString(KEY_SECURE, getSecureSignature())
            //putInt(KEY_API_VERSION, Billing.IN_APP_BILLING_VERSION)
        }
        return Intent().apply {
            `package` = NAIKA_SIGNER_PACKAGE_NAME
            putExtras(bundle)
        }
    }

    private fun createReceiverConnection() {
        receiverCommunicator = object : BillingReceiverCommunicator {
            override fun onNewBroadcastReceived(intent: Intent?) {
                onActionReceived(intent?.action!!, intent?.extras)
            }
        }
    }

    private fun onActionReceived(action: String, extras: Bundle?) {
        when (action) {
            ACTION_RECEIVE_BILLING_SUPPORT -> {
                onBillingSupportActionReceived(extras)
            }
            ACTION_RECEIVE_WALLET_CONNECT -> {
                onWalletConnect(extras)
            }
        }
    }

    private fun onBillingSupportActionReceived(extras: Bundle?) {
        val isResponseSucceed = isResponseSucceed(extras)

        when {
            isResponseSucceed -> {
                connectionCallbackReference?.get()?.connectionSucceed?.invoke()
            }
            !isResponseSucceed -> {
                connectionCallbackReference?.get()?.connectionFailed?.invoke(
                    IllegalArgumentException("Why")
                )
            }
            else -> {
                connectionCallbackReference?.get()?.connectionFailed?.invoke(
                    IllegalArgumentException("Why")
                )
            }
        }
    }

    private fun onWalletConnect(extras: Bundle?) {
        if (isResponseSucceed(extras)) {
            when {
                walletConnectWeakReference?.get() != null -> {
                    startWalletConnectActivity(
                        requireNotNull(walletConnectWeakReference?.get()),
                        getWalletConnectIntent(extras)
                    )
                }
                else -> {
                    // invalid state, we receive purchase but all reference is null, might be connection disconnected
                }
            }
        } else {
            getWalletConnectCallback()?.let { walletConnectCallback ->
                ConnectWalletCallback()
                    .apply(walletConnectCallback)
                    .connectWalletFailed.invoke(IllegalArgumentException("why!"))
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

    private fun startWalletConnectActivity(
        purchaseWeakHolder: WalletConnectWeakHolder,
        purchaseIntent: Intent?
    ) {
        purchaseWeakHolder.paymentLauncher.activityLauncher.launch(purchaseIntent)
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

    companion object {

        private const val ACTION_NAIKA_SIGNER_BASE = "io.naika.naikasigner."

        private const val ACTION_BILLING_SUPPORT = ACTION_NAIKA_SIGNER_BASE + "billingSupport"
        private const val ACTION_CONNECT_WALLET = ACTION_NAIKA_SIGNER_BASE + "connectWallet"

        private const val ACTION_RECEIVE_WALLET_CONNECT = "io.naika.naikapay.connectWallet"
        private const val ACTION_RECEIVE_BILLING_SUPPORT = "io.naika.naikapay.billingSupport"
        private const val KEY_PACKAGE_NAME = "packageName"

        private const val KEY_RESPONSE_CONNECT_WALLET_INTENT = "CONNECT_WALLET_INTENT"
    }

}