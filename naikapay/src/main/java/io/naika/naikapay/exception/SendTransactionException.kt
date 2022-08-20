package io.naika.naikapay.exception

class SendTransactionException(override val message: String) : IllegalStateException(message) {
}