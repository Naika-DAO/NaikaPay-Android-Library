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


const val SMART_CONTRACT_HASH_ADDRESS = "0xfff435b6d92e1cf601843ecbdbbc64da127a9bdd"
const val BUY_CHANCE_METHOD_HEX = "1734539f"
const val CLAIM_METHOD_HEX = "4e71d92d"
//0xcda753B61bF622c8475769202f9533820a1AD6f9

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    var currentRound = 0
    var predictedCoinState = 0
    var coinTossRounds = intArrayOf(-1, -1, -1)
    var chanceLeft = 0

    var isAccountConnected = false
    var selectedAddressHash = ""
    private val _tx = MutableLiveData<ByteArray>()
    val tx: LiveData<ByteArray> = _tx

    private val _claimTX = MutableLiveData<ByteArray>()
    val claimTX: LiveData<ByteArray> = _claimTX

    private val _transactionSucceed = MutableLiveData<Pair<Boolean, String>>()
    val transactionSucceed: LiveData<Pair<Boolean, String>> = _transactionSucceed

    private val _transactionFailed = MutableLiveData<String>()
    val transactionFailed: LiveData<String> = _transactionFailed

    var web3: Web3j? = null

    init {
        web3 =
            Web3j.build(HttpService("http://geth.naikadev.com:8545"))
    }

    fun connectToNetwork() {
        viewModelScope.launch {
            try {
                val web3ClientVersion = web3?.web3ClientVersion()?.sendAsync()?.get()
                if (!web3ClientVersion?.hasError()!!) {
                } else {
                    Log.d("Web3", web3ClientVersion.error.message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createClaimTransaction() {
        val ethGetTransactionCount: EthGetTransactionCount = web3?.ethGetTransactionCount(
            selectedAddressHash, DefaultBlockParameterName.LATEST
        )?.sendAsync()?.get()!!
        val gasPrice = web3?.ethGasPrice()?.sendAsync()?.get()
        val gasLimit = BigInteger("1000000")
        val data = CLAIM_METHOD_HEX
        val transaction = RawTransaction.createTransaction(
            ethGetTransactionCount.transactionCount,
            gasPrice?.gasPrice,
            gasLimit,
            SMART_CONTRACT_HASH_ADDRESS,
            BigInteger.ZERO,
            data
        )
        val byteArray = TransactionEncoder.encode(transaction, 5L)
        _claimTX.postValue(byteArray)
    }

    fun createTransaction() {
        val ethGetTransactionCount: EthGetTransactionCount = web3?.ethGetTransactionCount(
            selectedAddressHash, DefaultBlockParameterName.LATEST
        )?.sendAsync()?.get()!!
        val gasPrice = web3?.ethGasPrice()?.sendAsync()?.get()
        val gasLimit = BigInteger("1000000")
        val data = BUY_CHANCE_METHOD_HEX
        val transaction = RawTransaction.createTransaction(
            ethGetTransactionCount.transactionCount,
            gasPrice?.gasPrice,
            gasLimit,
            SMART_CONTRACT_HASH_ADDRESS,
            BigInteger("1000000000000000"),
            data
        )
        val byteArray = TransactionEncoder.encode(transaction, 5L)
        _tx.postValue(byteArray)
    }

    fun loadContract(signedTxHash: ByteArray?, isClaim: Boolean) {
        val txSignedHash = Numeric.toHexString(signedTxHash)
        val res = web3?.ethSendRawTransaction(txSignedHash)?.sendAsync()?.get()
        res?.let {
            it.error?.let { error ->
                _transactionFailed.postValue(error.message)
            }
            it.result?.let { result ->
                if (!isClaim) {
                    chanceLeft = 1
                }
                _transactionSucceed.postValue(Pair(isClaim, result))
            }
        }
/*        val coinToss = CoinToss.load(
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
        )*/

    }


}