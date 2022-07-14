package io.naika.naikapay.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.naika.naikapay.R
import org.ethereum.geth.Transaction
import java.math.BigDecimal
import java.math.BigInteger

const val ACCOUNT_ADDRESS_HASH = "address_hash"
const val ACCOUNT_ADDRESS_BALANCE = "address_balance"
const val SIGNED_TX_HASH = "signed_tx_hash"

const val ACTION_GET_ACCOUNT = "get_account"
const val ACTION_SIGN_TX = "sign_tx"

const val EXTRA_PLAIN_TRANSACTION_HASH = "tx_hash"
const val EXTRA_SIGNER_ADDRESS_HASH = "signer_hash"

class WalletActivity : AppCompatActivity(), WalletDialogFragment.WalletDialogFragmentInteraction {

    private lateinit var accountViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]

        when (intent.action) {
            ACTION_GET_ACCOUNT -> {
                val walletDialogFragment = WalletDialogFragment()
                walletDialogFragment.show(supportFragmentManager, "Wallet")
            }
            ACTION_SIGN_TX -> {
                val signerHash = intent.extras?.getString(EXTRA_SIGNER_ADDRESS_HASH)
                val txHash = intent.extras?.getByteArray(EXTRA_PLAIN_TRANSACTION_HASH)
                accountViewModel.signTx(Transaction(txHash), signerHash!!)
            }
        }

        accountViewModel.signedTx.observe(this) { tx ->
            val dataIntent = Intent()
            val rlp = tx.encodeRLP()

            dataIntent.putExtra(SIGNED_TX_HASH, rlp)

            setResult(Activity.RESULT_OK, dataIntent)
            finish()
        }


    }

    override fun onAccountSelected(accountInfoModel: AccountInfoModel) {
        supportFragmentManager.popBackStack()
        Log.d("WalletActivity", accountInfoModel.account.address.hex)
        val balanceInBigInteger = BigInteger(accountInfoModel.balance?.bytes)
        val balanceInBigDecimal = BigDecimal(balanceInBigInteger)
        val resBalance = balanceInBigDecimal.divide(BigDecimal("1000000000000000000"))
        val dataIntent = Intent()
        dataIntent.putExtra(ACCOUNT_ADDRESS_HASH, accountInfoModel.account.address.hex)
        dataIntent.putExtra(ACCOUNT_ADDRESS_BALANCE , resBalance.toDouble())

        setResult(Activity.RESULT_OK, dataIntent)
    }

    override fun onDismissCalled() {
        finish()
    }
}