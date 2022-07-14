package io.naika.naikapay

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ethereum.geth.*

class Wallet(context: Context) {

    private val ks: KeyStore =
        KeyStore("${context.filesDir}/keystore", Geth.LightScryptN, Geth.LightScryptP)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun createNewAccount(context: Context, passPhrase: String): Account {
        // Create a new account with the specified encryption passphrase.
        val newAccount = ks.newAccount(passPhrase)
        Log.d("address", newAccount.address.hex)
        return newAccount
    }

    suspend fun getListOfAccounts(): List<Account> {
        return withContext(Dispatchers.IO) {
            val accountList = mutableListOf<Account>()
            for (i in 0 until ks.accounts.size()) {
                accountList.add(i.toInt(), ks.accounts.get(i))
            }
            accountList
        }
    }

    fun isAddressExist(address: Address): Boolean {
        return ks.hasAddress(address)
    }

    fun exportAccount(account: Account, passPhrase: String, exportPassword: String): ByteArray? {
        // Export the newly created account with a different passphrase. The returned
        // data from this method invocation is a JSON encoded, encrypted key-file.
        return ks.exportKey(account, passPhrase, exportPassword)
    }

    fun changePassword(account: Account, currentPassPhrase:String, newPassPhrase:String){
        // Update the passphrase on the account created above inside the local keystore.
        ks.updateAccount(account, currentPassPhrase, newPassPhrase)
    }

    fun deleteAccount(account: Account, passPhrase: String){
        // Delete the account updated above from the local keystore.
        ks.deleteAccount(account, passPhrase)
    }

    fun importAccount(jsonAcc: ByteArray, exportPassword: String, importPassword: String): Account {
        // Import back the account we've exported (and then deleted) above with yet
        // again a fresh passphrase.
        return ks.importKey(jsonAcc, exportPassword, importPassword)
    }

    fun signTransaction(tx:Transaction, signer:Account,passPhrase: String, chainId:Long):Transaction{
        var signed: Transaction = ks.signTxPassphrase(signer, passPhrase, tx, BigInt(chainId))
        return signed

        // Sign a transaction with multiple manually cancelled authorizations
        //ks.unlock(signer, "Signer password")
        //signed = ks.signTx(signer, tx, chain)
        //ks.lock(signer.address)


        // Sign a transaction with multiple automatically cancelled authorizations
        //ks.timedUnlock(signer, "Signer password", 1000000000)
        //signed = ks.signTx(signer, tx, chain)


        // nonce, recipientAddr, amount, gasLimit, gasPrice, data
/*        val tx = Transaction(
            nonce, Address("0x0000000000000000000000000000000000000000"),
            BigInt(1000000007), estimateGas, suggestedGasPrice, null
        )*/
    }

}