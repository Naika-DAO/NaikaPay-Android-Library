package com.example.android.goeth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.android.goeth.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.naika.naikapay.*
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

    private val payment by lazy(LazyThreadSafetyMode.NONE) {
        Payment(context = this)
    }

    private lateinit var paymentConnection: Connection


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startPaymentConnection()

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

                if (paymentConnection.getState() == ConnectionState.Connected) {
                    connectWalletWithNewMethod()
                }
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

            sendSignTxRequest(tx)
        }

        mainViewModel.claimTX.observe(this) { tx ->
            sendSignTxRequest(tx)
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

    private fun sendSignTxRequest(tx: ByteArray) {
        payment.signTransaction(
            registry = activityResultRegistry,
            tx,
            mainViewModel.selectedAddressHash,
            CoinToss.ABI
        ) {
            signTransactionSucceed { signTransactionResponse ->
                Log.d("Payment", signTransactionResponse.signedTxByteArray.toString())
                val txSignedHexString =
                    Numeric.toHexString(signTransactionResponse.signedTxByteArray)
                val transaction = TransactionDecoder.decode(txSignedHexString)
                val isClaim = transaction.data == CLAIM_METHOD_HEX
                //mainViewModel.loadContract(signTransactionResponse.signedTxByteArray, isClaim)
                payment.sendTransaction(signTransactionResponse.signedTxByteArray) {
                    sendTransactionSucceed {
                        Log.d("Payment", it.txHash)
                    }
                    sendTransactionFailed {
                        Log.d("Payment", it.message!!)
                    }
                }
            }
            signTransactionCanceled {
                Log.d("Payment", "signTransactionCanceled")
            }
            signTransactionFailed { reason ->
                Log.d("Payment", reason.message.toString())
            }
        }
    }

    private fun startPaymentConnection() {
        paymentConnection = payment.initialize(NetworkType.ETH_MAIN) {
            connectionSucceed {
                Log.d("Payment", "connectionSucceed")

            }
            connectionFailed {
                Log.d("Payment", it.message.toString())
            }
            disconnected {
                Log.d("Payment", "disconnected")
            }
        }
    }

    private fun connectWalletWithNewMethod() {
        payment.connectWallet(
            registry = activityResultRegistry
        ) {
            connectWalletSucceed { accountInfo ->
                Log.d("Payment", "connectWalletSucceed")
                mainViewModel.isAccountConnected = true
                mainViewModel.selectedAddressHash = accountInfo.address
                binding.connectWalletButton.text = toSummarisedAddress(accountInfo.address)
                binding.chancesTextView.visibility = View.VISIBLE
                binding.buyChancesButton.isEnabled = true

                payment.getGasPrice {
                    gasPriceSucceed {
                        Log.d("Payment", it.gasPrice.toString())
                    }
                    gasPriceFailed {
                        Log.d("Payment", it.message!!)
                    }
                }

            }
            connectWalletCanceled {
                Log.d("Payment", "connectWalletCanceled")

            }
            connectWalletFailed {
                Log.d("Payment", it.message!!)
            }
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

    override fun onDestroy() {
        paymentConnection.disconnect()
        super.onDestroy()
    }
}