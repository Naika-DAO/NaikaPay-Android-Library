package io.naika.naikapay

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.ethereum.geth.BigInt
import org.komputing.khash.keccak.KeccakParameter
import org.komputing.khash.keccak.extensions.digestKeccak
import org.komputing.khex.extensions.hexToByteArray
import org.komputing.khex.model.HexString
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
    val balanceInBigInteger = BigInteger(1, balance.bytes)
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

fun getMethodName(abiString: String, methodHex: String): String {
    val moshi: Moshi = Moshi.Builder().build()
    val types = Types.newParameterizedType(
        List::class.java,
        ABI::class.java
    )
    val jsonAdapter: JsonAdapter<List<ABI>> = moshi.adapter(types)
    val abi = jsonAdapter.fromJson(abiString)
    abi?.let {
        it.forEach { item ->
            if (item.type == "function") {
                var methodNameByteArray = item.name.digestKeccak(KeccakParameter.KECCAK_256)
                if (item.inputs.isEmpty()) {
                    methodNameByteArray =
                        String.format("%s()", item.name).digestKeccak(KeccakParameter.KECCAK_256)
                            .sliceArray(0..3)
                }
                if (HexString(methodHex).hexToByteArray()
                        .contentEquals(methodNameByteArray)
                ) {
                    return item.name
                }
            }
        }
    }
    return ""

}
