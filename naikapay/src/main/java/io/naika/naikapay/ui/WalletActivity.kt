package io.naika.naikapay.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.naika.naikapay.R
import org.ethereum.geth.Account
import org.ethereum.geth.Geth
import org.ethereum.geth.Strings
import java.math.BigDecimal
import java.math.BigInteger

const val ACCOUNT_ADDRESS_HASH = "address_hash"
const val ACCOUNT_ADDRESS_BALANCE = "address_balance"

class WalletActivity : AppCompatActivity(), WalletDialogFragment.WalletDialogFragmentInteraction {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        val walletDialogFragment = WalletDialogFragment()
        walletDialogFragment.show(supportFragmentManager , "Wallet")

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