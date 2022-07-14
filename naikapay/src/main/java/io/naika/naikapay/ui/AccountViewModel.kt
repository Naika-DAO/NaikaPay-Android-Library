package io.naika.naikapay.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.naika.naikapay.EthClient
import io.naika.naikapay.Wallet
import kotlinx.coroutines.launch
import org.ethereum.geth.Account
import org.ethereum.geth.BigInt
import org.ethereum.geth.Transaction
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.set


class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val _accountList = MutableLiveData<List<Account>>()
    val accountList: LiveData<List<Account>> = _accountList
    val balanceHashMap: MutableMap<String, BigInt> = HashMap()

    private val _signedTx = MutableLiveData<Transaction>()
    val signedTx: LiveData<Transaction> = _signedTx

    private val wallet: Wallet = Wallet(application)
    private val ethClient = EthClient()

    fun getAccountList() {
        viewModelScope.launch {
            val accountList = wallet.getListOfAccounts()
            getAccountBalance(accountList)
            _accountList.postValue(accountList)
        }
    }

    fun getAccountBalance(accounts:List<Account>){
        viewModelScope.launch {
            accounts.forEach { account ->
                val balance = ethClient.getAddressBalance(account.address)
                if (balance != null) {
                    Log.d("balance" , balance.string())
                    balanceHashMap[account.address.hex] = balance
                }
            }

        }
    }

    fun createAccountIfNothingExist(context: android.content.Context){
        viewModelScope.launch {
            val accountList = wallet.getListOfAccounts()
            if (accountList.isEmpty()){
                wallet.createNewAccount(context, "123456")
            }
        }
    }

    fun signTx(tx: Transaction, addressHash: String) {
        viewModelScope.launch {
            val account = wallet.getListOfAccounts().find {
                it.address.hex == addressHash
            }
            val signedTx = wallet.signTransaction(tx, account!!, "Creation password", 5)
            //ethClient.sendTransaction(signedTx)
            _signedTx.postValue(signedTx)
        }
    }


}