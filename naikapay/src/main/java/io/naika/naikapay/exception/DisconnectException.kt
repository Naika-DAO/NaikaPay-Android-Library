package io.naika.naikapay.exception

class DisconnectException : IllegalStateException() {

    override val message: String?
        get() = "Could not connect to Naika Signer!"

}