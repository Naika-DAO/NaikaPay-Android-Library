package io.naika.naikapay

import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import io.naika.naikapay.callback.ConnectWalletCallback
import io.naika.naikapay.callback.ConnectionCallback

class Payment(
    context: Context
) {

    private val walletConnectResultParser = WalletConnectResultParser()
    private val connection = BillingConnection(context = context, walletConnectResultParser)


    fun initialize(callback: ConnectionCallback.() -> Unit): Connection {
        return connection.startConnection(callback)
    }

    fun connectWallet(
        registry: ActivityResultRegistry,
        callback: ConnectWalletCallback.() -> Unit
    ) {
        connection.connectWallet(registry, callback)
    }


}