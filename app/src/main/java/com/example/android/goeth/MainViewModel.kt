package com.example.android.goeth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthGetTransactionCount
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigInteger
import javax.inject.Inject


const val SMART_CONTRACT_HASH_ADDRESS = "0xcda753B61bF622c8475769202f9533820a1AD6f9"

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    var currentRound = 0
    var predictedCoinState = 0
    var coinTossRounds = intArrayOf(-1, -1, -1)

    var isAccountConnected = false
    var selectedAddressHash = ""
    private val _tx = MutableLiveData<ByteArray>()
    val tx: LiveData<ByteArray> = _tx

    var web3: Web3j? = null

    init {
        web3 =
            Web3j.build(HttpService("http://geth.naikadev.com:8545"));
    }

    fun connectToNetwork() {
        viewModelScope.launch {
            try {
                val web3ClientVersion = web3?.web3ClientVersion()?.sendAsync()?.get()
                if (!web3ClientVersion?.hasError()!!) {
                    val res = web3?.ethGetBalance(
                        "0xee912313d041e374b63081FBF4bb847ee55DE6dd",
                        DefaultBlockParameterName.LATEST
                    )?.sendAsync()?.get()
                    Log.d("Fuck", res?.balance.toString())
                } else {
                    Log.d("Web3", web3ClientVersion.error.message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createTransaction() {
        val ethGetTransactionCount: EthGetTransactionCount = web3?.ethGetTransactionCount(
            selectedAddressHash, DefaultBlockParameterName.LATEST
        )?.sendAsync()?.get()!!
        val gasPrice = web3?.ethGasPrice()?.sendAsync()?.get()
        val gasLimit = BigInteger("2000000")
        val data = "6057361d0000000000000000000000000000000000000000000000000000000000000150"
        val transaction = RawTransaction.createTransaction(
            ethGetTransactionCount.transactionCount,
            gasPrice?.gasPrice,
            gasLimit,
            SMART_CONTRACT_HASH_ADDRESS,
            data
        )
//        val transaction = RawTransaction.createContractTransaction(
//            ethGetTransactionCount.transactionCount,
//            gasPrice?.gasPrice,
//            gasLimit,
//            BigInteger("0"),
//            data
//        )
        val byteArray = TransactionEncoder.encode(transaction, 5L)
//        var transactionHash = Numeric.toHexString(byteArray)
//        transactionHash = transactionHash.drop(2)
        _tx.postValue(byteArray)
    }

    fun loadContract(signedTxHash: ByteArray?) {
        val txSignedHash = Numeric.toHexString(signedTxHash)
        val res = web3?.ethSendRawTransaction(txSignedHash)?.sendAsync()?.get()
        res?.let {
            it.error?.let { error ->
                Log.d("Fuck", error.message)
            }
            it.result?.let { result ->
                Log.d("Fuck", result)
            }
        }

/*        val storage = Storage.load(
            SMART_CONTRACT_HASH_ADDRESS,
            web3,
            rawTransaction,
            object : ContractGasProvider {
                override fun getGasPrice(contractFunc: String?): BigInteger {
                    return BigInteger.valueOf(22_000_000_000L)
                }

                override fun getGasPrice(): BigInteger {
                    return BigInteger.valueOf(22_000_000_000L)
                }

                override fun getGasLimit(contractFunc: String?): BigInteger {
                    return BigInteger.valueOf(4_300_000);
                }

                override fun getGasLimit(): BigInteger {
                    return BigInteger.valueOf(4_300_000);
                }

            }
        )
        storage.store(BigInteger("45"))*/
    }

/*    fun createTransaction() {
        val smartContractAddress = Geth.newAddressFromHex(SMART_CONTRACT_HASH_ADDRESS)
        val storage = Storage(smartContractAddress, ethClient)
        val transactOpt = TransactOpts()
        if (isAccountConnected) {
            transactOpt.from = Geth.newAddressFromHex(selectedAddressHash)
            transactOpt.nonce = ethClient.getNonceAt(Context(), transactOpt.from, -1)
            transactOpt.setContext(Context())
            transactOpt.gasLimit = 2_000_000_000L
                //ethClient.estimateGas(Context(), callMsg)
            transactOpt.gasPrice = ethClient.suggestGasPrice(Context())

            val data = "6057361d0000000000000000000000000000000000000000000000000000000000000142"
            val rawTx = storage.getRowTransaction(transactOpt, data.decodeHex())
            _tx.postValue(rawTx)

        }
    }

    fun storeToContract(signedTxHash: String?) {
        val smartContractAddress = Geth.newAddressFromHex(SMART_CONTRACT_HASH_ADDRESS)
        val storage = Storage(smartContractAddress, ethClient)
        val transactOpt = TransactOpts()
        if (isAccountConnected) {
            transactOpt.from = Geth.newAddressFromHex(selectedAddressHash)
            transactOpt.nonce = ethClient.getNonceAt(Context(), transactOpt.from, -1)
            transactOpt.setContext(Context())
            transactOpt.gasLimit = 2_000_000_000L
            //ethClient.estimateGas(Context(), callMsg)
            transactOpt.gasPrice = ethClient.suggestGasPrice(Context())
            transactOpt.setSigner { address, transaction ->
                Transaction(signedTxHash)
            }

        }
        val tx = storage.store(transactOpt, BigInt(322))
    }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }*/

}