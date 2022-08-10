package io.naika.naikapay

data class ABI(
    val inputs: List<Any>,
    val stateMutability: String,
    val type: String,
    val name: String,
    val outputs: List<Any>
)
