package com.example.android.goeth

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.android.goeth.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.naika.naikapay.toSummarisedAddress
import io.naika.naikapay.ui.*
import org.web3j.crypto.TransactionDecoder
import org.web3j.utils.Numeric

const val CHANCE_PRICE = 0.001

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    BuyChancesDialog.BuyChancesFragmentInteraction,
    PlayFragment.PlayFragmentInteraction,
    ClaimRewardDialogFragment.ClaimRewardFragmentInteraction {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val selectedAddress = data?.getStringExtra(ACCOUNT_ADDRESS_HASH)
            val balance = data?.getDoubleExtra(ACCOUNT_ADDRESS_BALANCE, 0.0)
            mainViewModel.isAccountConnected = true
            mainViewModel.selectedAddressHash = selectedAddress!!

        }
    }

    var signTxLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val signedTxHash = data?.getByteArrayExtra(SIGNED_TX_HASH)
                val txSignedHash = Numeric.toHexString(signedTxHash)
                val tx = TransactionDecoder.decode(txSignedHash)
                Log.d("Fuck", tx.data)
                val isClaim = tx.data == CLAIM_METHOD_HEX
                mainViewModel.loadContract(signedTxHash, isClaim)
            }
        }

    var chooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val selectedAddress = data?.getStringExtra(ACCOUNT_ADDRESS_HASH)
                val balance = data?.getDoubleExtra(ACCOUNT_ADDRESS_BALANCE, 0.0)
                mainViewModel.isAccountConnected = true
                mainViewModel.selectedAddressHash = selectedAddress!!
                binding.connectWalletButton.text = toSummarisedAddress(selectedAddress)
                binding.chancesTextView.visibility = View.VISIBLE
                binding.buyChancesButton.isEnabled = true

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mainViewModel.connectToNetwork()


        binding.connectWalletButton.setOnLongClickListener {
            if (mainViewModel.selectedAddressHash.isNullOrEmpty()) {
                return@setOnLongClickListener true
            }
            val clipboard: ClipboardManager? =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("address", mainViewModel.selectedAddressHash)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(this, "address copied to clip board", Toast.LENGTH_LONG).show()
            true
        }

        binding.connectWalletButton.setOnClickListener {
            if (mainViewModel.isAccountConnected) {
                val popUpMenu = PopupMenu(this, it)
                popUpMenu.menuInflater.inflate(R.menu.main_activity_menu, popUpMenu.menu)
                popUpMenu.setOnMenuItemClickListener {
                    disconnectAccount()
                    true
                }

                popUpMenu.show()
            } else {
                val intent: Intent? = Intent()
                intent?.setClassName(this, "io.naika.naikapay.ui.WalletActivity")
                intent?.action = ACTION_INTRO
                chooserLauncher.launch(intent)
            }
        }

        binding.playButton.setOnClickListener {


            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<PlayFragment>(R.id.fragment_container_view)
                addToBackStack("play")
            }
        }

        binding.buyChancesButton.setOnClickListener {
            val buyChancesDialog = BuyChancesDialog()
            buyChancesDialog.show(supportFragmentManager, "buyChances")
        }

        mainViewModel.tx.observe(this) { tx ->
            val intent: Intent? = Intent()
            intent?.setClassName(this, "io.naika.naikapay.ui.WalletActivity")
            intent?.action = ACTION_SIGN_TX
            intent?.putExtra(EXTRA_PLAIN_TRANSACTION_HASH, tx)
            intent?.putExtra(EXTRA_SIGNER_ADDRESS_HASH, mainViewModel.selectedAddressHash)
            signTxLauncher.launch(intent)
        }

        mainViewModel.claimTX.observe(this) { tx ->
            val intent: Intent? = Intent()
            intent?.setClassName(this, "io.naika.naikapay.ui.WalletActivity")
            intent?.action = ACTION_SIGN_TX
            intent?.putExtra(EXTRA_PLAIN_TRANSACTION_HASH, tx)
            intent?.putExtra(EXTRA_SIGNER_ADDRESS_HASH, mainViewModel.selectedAddressHash)
            signTxLauncher.launch(intent)
        }

        mainViewModel.transactionSucceed.observe(this) {
            if (!it.first) {
                binding.playButton.isEnabled = true
                binding.chancesTextView.text =
                    String.format("%d %s", mainViewModel.chanceLeft, "chance left")
            }
            val transactionResponseDialog = TransactionResponseDialog()
            val args = Bundle()
            args.putString(TX_HASH, it.second)
            transactionResponseDialog.arguments = args
            transactionResponseDialog.show(supportFragmentManager, "tx_success")
        }

        mainViewModel.transactionFailed.observe(this) {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }


    }

    private fun disconnectAccount() {
        mainViewModel.isAccountConnected = false
        mainViewModel.selectedAddressHash = ""
        binding.connectWalletButton.text = getString(R.string.connect_wallet)
        binding.chancesTextView.visibility = View.GONE
        binding.buyChancesButton.isEnabled = false
    }

    override fun onBuyChancesClicked(numberOfChances: Int) {
        mainViewModel.createTransaction()
    }

    override fun finishGameWithLost() {
        supportFragmentManager.popBackStack()
        binding.chancesTextView.text =
            String.format("%d %s", mainViewModel.chanceLeft, "chance left")
        binding.playButton.isEnabled = false
    }

    override fun claimRewardClicked() {
        supportFragmentManager.popBackStack()
        mainViewModel.createClaimTransaction()
        binding.chancesTextView.text =
            String.format("%d %s", mainViewModel.chanceLeft, "chance left")
        binding.playButton.isEnabled = false
    }
}