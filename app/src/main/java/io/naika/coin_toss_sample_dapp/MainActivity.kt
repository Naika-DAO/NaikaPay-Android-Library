package io.naika.coin_toss_sample_dapp

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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.naika.coin_toss_sample_dapp.databinding.ActivityMainBinding
import io.naika.naikapay.*


const val LOG_TAG = "Payment"
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
            if (mainViewModel.selectedAddressHash.isEmpty()) {
                return@setOnLongClickListener true
            }
            val clipboard: ClipboardManager? =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("address", mainViewModel.selectedAddressHash)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(this, getString(R.string.toast_text_address_copied), Toast.LENGTH_LONG)
                .show()
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

                //You can send transaction to the network by NaikaPay SDK or You can send it yourself with a thirdParty

                /*
                * Send transaction with web3j library
                * val txSignedHexString = Numeric.toHexString(signTransactionResponse.signedTxByteArray)
                * val transaction = TransactionDecoder.decode(txSignedHexString)
                * val isClaim = transaction.data == CLAIM_METHOD_HEX
                * mainViewModel.sendTransactionToNetwork(signTransactionResponse.signedTxByteArray, isClaim)
                * */

                /*
                * Send transaction with NaikaPay SDK
                * */
                payment.sendTransaction(signTransactionResponse.signedTxByteArray) {
                    sendTransactionSucceed {
                        Log.d(LOG_TAG, it.txHash)
                    }
                    sendTransactionFailed {
                        Log.d(LOG_TAG, it.message!!)
                    }
                }
            }
            signTransactionCanceled {
                Log.d(LOG_TAG, "signTransactionCanceled")
            }
            signTransactionFailed { reason ->
                Log.d(LOG_TAG, reason.message.toString())
            }
        }
    }

    private fun startPaymentConnection() {
        paymentConnection = payment.initialize(NetworkType.ETH_MAIN) {
            connectionSucceed {
                Log.d(LOG_TAG, "connectionSucceed")

            }
            connectionFailed {
                Log.d(LOG_TAG, it.message.toString())
            }
            disconnected {
                Log.d(LOG_TAG, "disconnected")
            }
        }
    }

    private fun connectWalletWithNewMethod() {
        payment.connectWallet(
            registry = activityResultRegistry
        ) {
            connectWalletSucceed { accountInfo ->
                Log.d(LOG_TAG, "connectWalletSucceed")
                mainViewModel.isAccountConnected = true
                mainViewModel.selectedAddressHash = accountInfo.address
                binding.connectWalletButton.text = toSummarisedAddress(accountInfo.address)
                binding.chancesTextView.visibility = View.VISIBLE
                binding.buyChancesButton.isEnabled = true

                payment.getGasPrice {
                    gasPriceSucceed {
                        Log.d(LOG_TAG, it.gasPrice.toString())
                    }
                    gasPriceFailed {
                        Log.d(LOG_TAG, it.message!!)
                    }
                }

            }
            connectWalletCanceled {
                Log.d(LOG_TAG, "connectWalletCanceled")

            }
            connectWalletFailed {
                Log.d(LOG_TAG, it.message!!)
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
            String.format(
                "%d %s",
                mainViewModel.chanceLeft,
                getString(R.string.general_text_chance_left)
            )
        binding.playButton.isEnabled = false
    }

    override fun claimRewardClicked() {
        supportFragmentManager.popBackStack()
        mainViewModel.createClaimTransaction()
        binding.chancesTextView.text =
            String.format(
                "%d %s",
                mainViewModel.chanceLeft,
                getString(R.string.general_text_chance_left)
            )
        binding.playButton.isEnabled = false
    }

    override fun onDestroy() {
        paymentConnection.disconnect()
        super.onDestroy()
    }
}