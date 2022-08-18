package io.naika.naikapay.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import io.naika.naikapay.R

const val ACCOUNT_ADDRESS_HASH = "address_hash"
const val ACCOUNT_ADDRESS_BALANCE = "address_balance"
const val SIGNED_TX_HASH = "signed_tx_hash"

const val ACTION_GET_ACCOUNT = "get_account"
const val ACTION_SIGN_TX = "sign_tx"
const val ACTION_INTRO = "intro"

const val EXTRA_PLAIN_TRANSACTION_HASH = "tx_hash"
const val EXTRA_SIGNER_ADDRESS_HASH = "signer_hash"

class WalletActivity : AppCompatActivity() {


    var chooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                setResult(Activity.RESULT_OK, result.data)
                finish()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)


        when (intent.action) {
            ACTION_INTRO -> {
                val intent: Intent? =
                    applicationContext.packageManager.getLaunchIntentForPackage("io.naika.naikasigner")
                //intent?.setClassName(this, "io.naika.naikasigner.ui.MainActivity")
                intent?.flags = 0
                intent?.action = ACTION_INTRO
                chooserLauncher.launch(intent)
            }
        }


    }


/*    override fun onDismissCalled() {
        finish()
    }

    override fun onConfirmClicked(signedTX: ByteArray) {
        val dataIntent = Intent()
        dataIntent.putExtra(SIGNED_TX_HASH, signedTX)
        setResult(Activity.RESULT_OK, dataIntent)
        finish()
    }

    override fun onWalletSelected(walletType: WalletChooserDialogFragment.WalletType) {
        when (walletType) {
            WalletChooserDialogFragment.WalletType.NAIKA_PAY -> {
                accountViewModel.walletType = WalletChooserDialogFragment.WalletType.NAIKA_PAY
                val walletDialogFragment = WalletDialogFragment()
                walletDialogFragment.show(supportFragmentManager, "Wallet")
            }
            WalletChooserDialogFragment.WalletType.METAMASK -> {
            }
        }
    }*/

}