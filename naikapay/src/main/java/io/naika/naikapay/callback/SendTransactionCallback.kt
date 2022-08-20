package io.naika.naikapay.callback

import io.naika.naikapay.entity.SendTransactionResponse

class SendTransactionCallback {

    internal var sendTransactionSucceed: (sendTransactionResponse: SendTransactionResponse) -> Unit =
        {}


    internal var sendTransactionFailed: (throwable: Throwable) -> Unit = {}

    fun sendTransactionSucceed(block: (sendTransactionResponse: SendTransactionResponse) -> Unit) {
        sendTransactionSucceed = block
    }

    fun sendTransactionFailed(block: (throwable: Throwable) -> Unit) {
        sendTransactionFailed = block
    }

}