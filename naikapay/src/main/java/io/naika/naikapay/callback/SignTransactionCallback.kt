package io.naika.naikapay.callback

import io.naika.naikapay.entity.SignTransactionResponse

class SignTransactionCallback {

    internal var signTransactionSucceed: (signTransactionResponse: SignTransactionResponse) -> Unit =
        {}

    internal var signTransactionCanceled: () -> Unit = {}

    internal var signTransactionFailed: (throwable: Throwable) -> Unit = {}

    fun signTransactionSucceed(block: (signTransactionResponse: SignTransactionResponse) -> Unit) {
        signTransactionSucceed = block
    }

    fun signTransactionCanceled(block: () -> Unit) {
        signTransactionCanceled = block
    }

    fun signTransactionFailed(block: (throwable: Throwable) -> Unit) {
        signTransactionFailed = block
    }

}