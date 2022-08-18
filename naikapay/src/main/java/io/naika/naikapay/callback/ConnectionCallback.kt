package io.naika.naikapay.callback

import io.naika.naikapay.Connection
import io.naika.naikapay.ConnectionState

class ConnectionCallback(private val disconnect: () -> Unit) : Connection {

    private var connectionState: ConnectionState = ConnectionState.Disconnected

    internal var connectionSucceed: () -> Unit = {}

    internal var connectionFailed: (throwable: Throwable) -> Unit = {}

    internal var disconnected: () -> Unit = {}

    fun connectionSucceed(block: () -> Unit) {
        connectionSucceed = {
            connectionState = ConnectionState.Connected
            block.invoke()
        }
    }

    fun connectionFailed(block: (throwable: Throwable) -> Unit) {
        connectionFailed = {
            connectionState = ConnectionState.FailedToConnect
            block.invoke(it)
        }
    }

    fun disconnected(block: () -> Unit) {
        disconnected = {
            connectionState = ConnectionState.Disconnected
            block.invoke()
        }
    }

    override fun getState(): ConnectionState {
        return connectionState
    }

    override fun disconnect() {
        disconnect.invoke()
    }

}
