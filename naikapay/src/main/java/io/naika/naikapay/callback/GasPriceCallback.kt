package io.naika.naikapay.callback

import io.naika.naikapay.entity.GasPriceResponse


class GasPriceCallback {
    internal var gasPriceSucceed: (gasPriceResponse: GasPriceResponse) -> Unit =
        {}


    internal var gasPriceFailed: (throwable: Throwable) -> Unit = {}

    fun gasPriceSucceed(block: (gasPriceResponse: GasPriceResponse) -> Unit) {
        gasPriceSucceed = block
    }

    fun gasPriceFailed(block: (throwable: Throwable) -> Unit) {
        gasPriceFailed = block
    }
}