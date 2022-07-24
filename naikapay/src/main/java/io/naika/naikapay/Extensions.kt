package io.naika.naikapay

import org.ethereum.geth.BigInt
import java.math.BigDecimal
import java.math.BigInteger

fun toSummarisedAddress(addressHash: String): String {
    val firstSection = addressHash.substring(0, 6)
    val dots = "....."
    val size = addressHash.length
    val lastSection = addressHash.subSequence(size - 6, size)
    return "$firstSection$dots$lastSection".toLowerCase()
}

fun convertBigIntBalanceToDouble(balance: BigInt?): Double {
    if (balance == null || balance.bytes == null) {
        return 0.0
    }
    val balanceInBigInteger = BigInteger(balance.bytes)
    val balanceInBigDecimal = BigDecimal(balanceInBigInteger)
    val resBalance = balanceInBigDecimal.divide(BigDecimal("1000000000000000000"))
    return resBalance.toDouble()
}

fun calculateGasFeeInETH(gasPrice: BigInt?, gasLimit: Long): Double {
    if (gasPrice == null || gasPrice.bytes == null) {
        return 0.0
    }
    val gasInBigInteger = BigInteger(gasPrice.bytes)
    val resInteger = gasInBigInteger.multiply(BigInteger(gasLimit.toString()))
    val resDecimal = BigDecimal(resInteger)
    val finalRes = resDecimal.divide(BigDecimal("1000000000000000000"))
    return finalRes.toDouble()
}