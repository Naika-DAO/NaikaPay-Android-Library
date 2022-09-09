package io.naika.naikapay


fun toSummarisedAddress(addressHash: String): String {
    val firstSection = addressHash.substring(0, 6)
    val dots = "....."
    val size = addressHash.length
    val lastSection = addressHash.subSequence(size - 6, size)
    return "$firstSection$dots$lastSection".lowercase()
}



