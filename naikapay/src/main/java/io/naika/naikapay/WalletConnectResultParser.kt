package io.naika.naikapay

import android.content.Intent
import android.util.Log
import io.naika.naikapay.callback.ConnectWalletCallback
import io.naika.naikapay.constant.NaikaSignerIntent
import io.naika.naikapay.entity.AccountInfo

internal class WalletConnectResultParser {

    fun handleReceivedResult(
        data: Intent?,
        walletCallback: ConnectWalletCallback.() -> Unit
    ) {
        Log.d("Payment", "handleReceivedResult")
        if (data?.extras?.get(NaikaSignerIntent.RESPONSE_CODE) == NaikaSignerIntent.RESPONSE_RESULT_OK) {
            parseResult(data, walletCallback)
        } else {
            ConnectWalletCallback().apply(walletCallback)
                .connectWalletFailed
                .invoke(IllegalStateException("Response code is not valid"))
        }
    }

    private fun parseResult(
        data: Intent?,
        walletCallback: ConnectWalletCallback.() -> Unit
    ) {
        val selectedAddress = data?.getStringExtra(NaikaSignerIntent.RESPONSE_SELECTED_ADDRESS_DATA)
        val balance = data?.getDoubleExtra(NaikaSignerIntent.RESPONSE_BALANCE_DATA, 0.0)
        if (selectedAddress != null && balance != null) {
            val accountInfo = AccountInfo(selectedAddress, balance)
            ConnectWalletCallback().apply(walletCallback)
                .connectWalletSucceed
                .invoke(accountInfo)
        } else {
            ConnectWalletCallback().apply(walletCallback)
                .connectWalletFailed
                .invoke(IllegalStateException("Received data is not valid"))
        }
    }

}