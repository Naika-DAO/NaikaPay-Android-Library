package io.naika.naikapay

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ethereum.geth.*

class EthClient {

    private val ethClient: EthereumClient = Geth.newEthereumClient("http://geth.naikadev.com:8545")


    suspend fun getAddressBalance(address: Address): BigInt? {
        return withContext(Dispatchers.IO) {
            ethClient.getBalanceAt(Context(), address, -1)
        }
    }

    fun sendTransaction(signedTransaction: Transaction) {
        ethClient.sendTransaction(Context(), signedTransaction)
    }

    fun getSuggestedGasPrice(): BigInt? {
        return ethClient.suggestGasPrice(Context())
    }

    fun getEstimateGas(): Long {
        return ethClient.estimateGas(Context(), CallMsg())
    }

    fun getNonceForAddress(address: Address): Long {
        return ethClient.getNonceAt(Context(), address, -1)
    }


}