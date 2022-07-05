package io.naika.naikapay

import org.ethereum.geth.*

class EthClient {

    private val ethClient: EthereumClient = Geth.newEthereumClient("http://geth.naikadev.com:8545")


    fun getAddressBalance(address: Address): BigInt? {
        return ethClient.getBalanceAt(Context(), address, -1)
    }

    fun sendTransaction(signedTransaction: Transaction){
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

    fun getTestSmartContract(): Storage {
        val address = Geth.newAddressFromHex("0xcda753B61bF622c8475769202f9533820a1AD6f9")
        return Storage(address, ethClient)
    }

}