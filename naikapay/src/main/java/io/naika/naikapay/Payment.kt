package io.naika.naikapay

import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import io.naika.naikapay.callback.ConnectWalletCallback
import io.naika.naikapay.callback.ConnectionCallback
import io.naika.naikapay.callback.SendTransactionCallback
import io.naika.naikapay.callback.SignTransactionCallback

class Payment(
    context: Context
) {

    private val walletConnectResultParser = WalletConnectResultParser()

    private val signTransactionResultParser = SignTransactionResultParser()

    private val connection =
        BillingConnection(context = context, walletConnectResultParser, signTransactionResultParser)


    fun initialize(callback: ConnectionCallback.() -> Unit): Connection {
        return connection.startConnection(callback)
    }

    fun connectWallet(
        registry: ActivityResultRegistry,
        callback: ConnectWalletCallback.() -> Unit
    ) {
        connection.connectWallet(registry, callback)
    }


    fun signTransaction(
        registry: ActivityResultRegistry,
        unsignedTx: ByteArray,
        selectedAccountHash: String,
        callback: SignTransactionCallback.() -> Unit
    ) {
        connection.signTransaction(
            registry,
            unsignedTx,
            selectedAccountHash,
            callback
        )
    }

    fun sendTransaction(
        signedTx: ByteArray,
        callback: SendTransactionCallback.() -> Unit
    ) {
        connection.sendTransaction(
            signedTx,
            callback
        )
    }

}