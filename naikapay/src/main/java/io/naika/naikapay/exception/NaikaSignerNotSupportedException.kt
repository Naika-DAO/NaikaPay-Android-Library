package io.naika.naikapay.exception

class NaikaSignerNotSupportedException : IllegalStateException() {

    override val message: String?
        get() = "Naika Signer is not updated"

}