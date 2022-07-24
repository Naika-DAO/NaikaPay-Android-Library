package io.naika.naikapay.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import io.naika.naikapay.EthClient
import io.naika.naikapay.Wallet
import io.naika.naikapay.server.BridgeServer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.ethereum.geth.Account
import org.ethereum.geth.Address
import org.ethereum.geth.BigInt
import org.ethereum.geth.Transaction
import org.komputing.khex.extensions.toNoPrefixHexString
import org.walletconnect.Session
import org.walletconnect.impls.*
import org.walletconnect.nullOnThrow
import java.io.File
import java.util.*


class AccountViewModel(application: Application) : AndroidViewModel(application) {

    var walletType: WalletChooserDialogFragment.WalletType =
        WalletChooserDialogFragment.WalletType.NAIKA_PAY

    private val _accountList = MutableLiveData<List<AccountUIModel>>()
    val accountList: LiveData<List<AccountUIModel>> = _accountList

    private val _accountListChanged = MutableLiveData<List<AccountUIModel>>()
    val accountListChanged: LiveData<List<AccountUIModel>> = _accountListChanged

    private val _signedTx = MutableLiveData<Transaction>()
    val signedTx: LiveData<Transaction> = _signedTx

    private val wallet: Wallet = Wallet(application)
    private val ethClient = EthClient()

    private lateinit var client: OkHttpClient
    private lateinit var moshi: Moshi
    private lateinit var bridge: BridgeServer
    private lateinit var storage: WCSessionStore
    lateinit var config: Session.Config
    lateinit var session: Session

    init {
        initMoshi()
        initClient()
        initBridge()
        initSessionStorage(application)
    }

    private fun initClient() {
        client = OkHttpClient.Builder().build()
    }

    private fun initMoshi() {
        moshi = Moshi.Builder().build()
    }


    private fun initBridge() {
        bridge = BridgeServer(moshi)
        bridge.start()
    }

    private fun initSessionStorage(application: Application) {
        storage = FileWCSessionStore(
            File(
                application.cacheDir,
                "session_store.json"
            ).apply { createNewFile() }, moshi
        )
    }

    fun resetSession() {
        nullOnThrow { session }?.clearCallbacks()
        val key = ByteArray(32).also { Random().nextBytes(it) }.toNoPrefixHexString()
        config = Session.Config(
            UUID.randomUUID().toString(),
            "http://localhost:${BridgeServer.PORT}",
            key
        )
        session = WCSession(
            config,
            MoshiPayloadAdapter(moshi),
            storage,
            OkHttpTransport.Builder(client, moshi),
            Session.PeerMeta(name = "Example App")
        )
        session.offer()
    }

    fun getAccountList() {
        viewModelScope.launch {
            val accountList = wallet.getListOfAccounts()
            getAccountBalance(accountList)
            val accountUIModelList = mutableListOf<AccountUIModel>()
            accountList.forEach {
                accountUIModelList.add(AccountUIModel(it))
            }
            _accountList.postValue(accountUIModelList)
        }
    }

    fun getSingleAddressBalance(address: Address): BigInt? {
        return runBlocking {
            val balance = ethClient.getAddressBalance(address)
            return@runBlocking balance
        }
    }

    private fun getAccountBalance(accounts: List<Account>) {
        viewModelScope.launch {
            accounts.forEach { account ->
                val balance = ethClient.getAddressBalance(account.address)
                if (balance != null) {
                    val accountUIModelList = _accountList.value
                    accountUIModelList?.find {
                        it.account.address.hex == account.address.hex
                    }?.balance = balance
                    _accountListChanged.postValue(accountUIModelList)

                    Log.d("balance", balance.string())
                }
            }

        }
    }

    fun createAccountIfNothingExist(context: android.content.Context) {
        viewModelScope.launch {
            val accountList = wallet.getListOfAccounts()
            if (accountList.isEmpty()) {
                wallet.createNewAccount(context, "123456")
            }
        }
    }

    fun getEstimateGasFee(tx: Transaction): Long {
        return ethClient.getEstimateGas(tx)
    }

    fun getGasPrice(): BigInt? {
        return ethClient.getSuggestedGasPrice()
    }

    fun signTx(tx: Transaction, addressHash: String) {
        viewModelScope.launch {
            if (walletType == WalletChooserDialogFragment.WalletType.NAIKA_PAY) {
                val account = wallet.getListOfAccounts().find {
                    it.address.hex == addressHash
                }
                val signedTx = wallet.signTransaction(tx, account!!, "Creation password", 5)
                _signedTx.postValue(signedTx)
            } else {
                val from = session.approvedAccounts()?.first()
                val txRequest = System.currentTimeMillis()
                //val methodCall = Session.MethodCall.Custom(txRequest, tx.)
                //session.performMethodCall()
            }
        }
    }

    private fun handleResponse(resp: Session.MethodCall.Response) {
//        if (resp.id == txRequest) {
//            txRequest = null
//        }
    }


}