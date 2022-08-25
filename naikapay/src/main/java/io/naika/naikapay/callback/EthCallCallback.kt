package io.naika.naikapay.callback

import io.naika.naikapay.entity.EthCallResponse

class EthCallCallback {

    internal var ethCallSucceed: (ethCallResponse: EthCallResponse) -> Unit =
        {}


    internal var ethCallFailed: (throwable: Throwable) -> Unit = {}

    fun ethCallSucceed(block: (ethCallResponse: EthCallResponse) -> Unit) {
        ethCallSucceed = block
    }

    fun gasPriceFailed(block: (throwable: Throwable) -> Unit) {
        ethCallFailed = block
    }

}