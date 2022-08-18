package io.naika.naikapay.receiver

import android.content.Intent

interface BillingReceiverCommunicator {
    fun onNewBroadcastReceived(intent: Intent?)
}
