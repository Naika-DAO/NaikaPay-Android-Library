package io.naika.naikapay.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import io.naika.naikapay.EthClient
import io.naika.naikapay.Wallet
import kotlinx.coroutines.launch
import org.ethereum.geth.*


class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val _accountList = MutableLiveData<List<Account>>()
    val accountList: LiveData<List<Account>> = _accountList
    val balanceHashMap:MutableMap<String, BigInt> = HashMap()

    private val wallet: Wallet = Wallet(application)
    private val ethClient = EthClient()

    fun getAccountList(){
        viewModelScope.launch {
            retrieve()
            val accountList = wallet.getListOfAccounts()
            _accountList.postValue(accountList)
            getAccountBalance(accountList)
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

    fun retrieve(){
        viewModelScope.launch {
            val storage = ethClient.getTestSmartContract()
            val res = storage.retrieve(null)
            Log.d("Storage" , res.string())
        }
    }


}