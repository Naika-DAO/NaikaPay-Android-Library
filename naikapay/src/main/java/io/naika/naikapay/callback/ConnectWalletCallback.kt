package io.naika.naikapay.callback

import io.naika.naikapay.entity.AccountInfo

class ConnectWalletCallback {

    internal var connectWalletSucceed: (accountInfo: AccountInfo) -> Unit = {}

    internal var connectWalletCanceled: () -> Unit = {}

    internal var connectWalletFailed: (throwable: Throwable) -> Unit = {}

    fun connectWalletSucceed(block: (accountInfo: AccountInfo) -> Unit) {
        connectWalletSucceed = block
    }

    fun connectWalletCanceled(block: () -> Unit) {
        connectWalletCanceled = block
    }

    fun connectWalletFailed(block: (throwable: Throwable) -> Unit) {
        connectWalletFailed = block
    }

}