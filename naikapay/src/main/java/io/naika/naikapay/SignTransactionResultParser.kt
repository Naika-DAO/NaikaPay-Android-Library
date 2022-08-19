package io.naika.naikapay

import android.content.Intent
import android.util.Log
import io.naika.naikapay.callback.SignTransactionCallback
import io.naika.naikapay.constant.NaikaSignerIntent
import io.naika.naikapay.entity.SignTransactionResponse

internal class SignTransactionResultParser {

    fun handleReceivedResult(
        data: Intent?,
        signTransactionCallback: SignTransactionCallback.() -> Unit
    ) {
        Log.d("Payment", "handleReceivedResult")
        if (data?.extras?.get(NaikaSignerIntent.RESPONSE_CODE) == NaikaSignerIntent.RESPONSE_RESULT_OK) {
            parseResult(data, signTransactionCallback)
        } else {
            SignTransactionCallback().apply(signTransactionCallback)
                .signTransactionFailed
                .invoke(IllegalStateException("Response code is not valid"))
        }
    }

    private fun parseResult(
        data: Intent?,
        signTransactionCallback: SignTransactionCallback.() -> Unit
    ) {
        val signedTxByteArray =
            data?.getByteArrayExtra(NaikaSignerIntent.RESPONSE_SIGNED_TRANSACTION_BYTES)

        if (signedTxByteArray != null && signedTxByteArray.isNotEmpty()) {
            val signTransactionResponse = SignTransactionResponse(signedTxByteArray)
            SignTransactionCallback().apply(signTransactionCallback)
                .signTransactionSucceed
                .invoke(signTransactionResponse)
        } else {
            SignTransactionCallback().apply(signTransactionCallback)
                .signTransactionFailed
                .invoke(IllegalStateException("Received data is not valid"))
        }
    }
}