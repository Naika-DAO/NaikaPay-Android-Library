package io.naika.naikapay

import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import io.naika.naikapay.callback.*

class Payment(
    context: Context
) {

    private val walletConnectResultParser = WalletConnectResultParser()

    private val signTransactionResultParser = SignTransactionResultParser()

    private val connection =
        BillingConnection(context = context, walletConnectResultParser, signTransactionResultParser)


    fun initialize(networkType: NetworkType, callback: ConnectionCallback.() -> Unit): Connection {
        return connection.startConnection(networkType, callback)
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
        abi: String,
        callback: SignTransactionCallback.() -> Unit
    ) {
        connection.signTransaction(
            registry,
            unsignedTx,
            selectedAccountHash,
            abi,
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

    fun getGasPrice(
        callback: GasPriceCallback.() -> Unit
    ) {
        connection.getGasPrice(
            callback
        )
    }

    fun ethCall(
        from: String,
        to: String,
        data: String,
        callback: EthCallCallback.() -> Unit
    ) {
        connection.ethCall(
            from,
            to,
            data,
            callback
        )
    }

}