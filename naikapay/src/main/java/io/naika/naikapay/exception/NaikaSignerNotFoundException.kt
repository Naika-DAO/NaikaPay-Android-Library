package io.naika.naikapay.exception

class NaikaSignerNotFoundException : IllegalStateException() {

    override val message: String?
        get() = "Naika Signer is not installed"

}