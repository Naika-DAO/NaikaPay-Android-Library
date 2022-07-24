package io.naika.naikapay.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.naika.naikapay.R
import io.naika.naikapay.convertBigIntBalanceToDouble
import kotlinx.coroutines.launch
import org.ethereum.geth.Geth
import org.walletconnect.Session
import org.walletconnect.nullOnThrow

const val ACCOUNT_ADDRESS_HASH = "address_hash"
const val ACCOUNT_ADDRESS_BALANCE = "address_balance"
const val SIGNED_TX_HASH = "signed_tx_hash"

const val ACTION_GET_ACCOUNT = "get_account"
const val ACTION_SIGN_TX = "sign_tx"
const val ACTION_INTRO = "intro"

const val EXTRA_PLAIN_TRANSACTION_HASH = "tx_hash"
const val EXTRA_SIGNER_ADDRESS_HASH = "signer_hash"

class WalletActivity : AppCompatActivity(), WalletDialogFragment.WalletDialogFragmentInteraction,
    WalletChooserDialogFragment.WalletChooserDialogFragmentInteraction,
    SignDialogFragment.SignDialogFragmentInteraction, Session.Callback {

    private lateinit var accountViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]

        when (intent.action) {
            ACTION_INTRO -> {
                val walletChooserDialogFragment = WalletChooserDialogFragment()
                walletChooserDialogFragment.show(supportFragmentManager, "Chooser")
            }
            ACTION_GET_ACCOUNT -> {
                val walletDialogFragment = WalletDialogFragment()
                walletDialogFragment.show(supportFragmentManager, "Wallet")
            }
            ACTION_SIGN_TX -> {

                val signerHash = intent.extras?.getString(EXTRA_SIGNER_ADDRESS_HASH)
                val txHash = intent.extras?.getByteArray(EXTRA_PLAIN_TRANSACTION_HASH)
                val balance =
                    accountViewModel.getSingleAddressBalance(Geth.newAddressFromHex(signerHash))

                val signDialogFragment = SignDialogFragment()
                val args = Bundle()
                args.putByteArray(SIGN_DIALOG_TX, txHash)
                args.putString(SIGN_DIALOG_ADDRESS, signerHash)
                args.putDouble(SIGN_DIALOG_ADDRESS_BALANCE, convertBigIntBalanceToDouble(balance))
                signDialogFragment.arguments = args
                signDialogFragment.show(supportFragmentManager, "sign")
            }
        }


    }

    override fun onAccountSelected(accountInfoModel: AccountInfoModel) {
        supportFragmentManager.popBackStack()
        Log.d("WalletActivity", accountInfoModel.account.address.hex)
        val balance = convertBigIntBalanceToDouble(accountInfoModel.balance!!)
        sendConnectResult(accountInfoModel.account.address.hex, balance)

    }

    private fun sendConnectResult(address: String?, balance: Double) {
        val dataIntent = Intent()
        dataIntent.putExtra(ACCOUNT_ADDRESS_HASH, address)
        dataIntent.putExtra(ACCOUNT_ADDRESS_BALANCE, balance)

        setResult(Activity.RESULT_OK, dataIntent)
    }

    override fun onDismissCalled() {
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
                accountViewModel.walletType = WalletChooserDialogFragment.WalletType.METAMASK
                initialSetup()
                accountViewModel.resetSession()
                accountViewModel.session.addCallback(this)
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(accountViewModel.config.toWCUri())
                Log.d("Babak", accountViewModel.config.toWCUri())
                startActivity(i)
            }
        }
    }

    private fun initialSetup() {
        val session = nullOnThrow { accountViewModel.session } ?: return
        session.addCallback(this)
        sessionApproved()
    }

    override fun onMethodCall(call: Session.MethodCall) {

    }

    override fun onStatus(status: Session.Status) {
        when (status) {
            Session.Status.Approved -> sessionApproved()
            Session.Status.Closed -> sessionClosed()
            Session.Status.Connected,
            Session.Status.Disconnected,
            is Session.Status.Error -> {
                Log.d("MetaMask", status.toString())
            }
        }
    }

    private fun sessionApproved() {
        lifecycleScope.launch {
            Log.d("Babak", "Connected: ${accountViewModel.session.approvedAccounts()}")
            sendConnectResult(accountViewModel.session.approvedAccounts()?.get(0), 0.085)
            finish()
        }
    }

    private fun sessionClosed() {
        lifecycleScope.launch {

        }
    }
}