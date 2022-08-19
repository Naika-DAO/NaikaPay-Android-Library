package io.naika.naikapay.sign_transaction

import io.naika.naikapay.PaymentLauncher
import io.naika.naikapay.callback.SignTransactionCallback

internal data class SignTransactionWeakHolder(
    val paymentLauncher: PaymentLauncher,
    val callback: SignTransactionCallback.() -> Unit
)