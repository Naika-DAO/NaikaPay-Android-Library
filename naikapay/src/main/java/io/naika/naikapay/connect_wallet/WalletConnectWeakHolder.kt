package io.naika.naikapay.connect_wallet

import io.naika.naikapay.PaymentLauncher
import io.naika.naikapay.callback.ConnectWalletCallback


internal data class WalletConnectWeakHolder(
    val paymentLauncher: PaymentLauncher,
    val callback: ConnectWalletCallback.() -> Unit
)