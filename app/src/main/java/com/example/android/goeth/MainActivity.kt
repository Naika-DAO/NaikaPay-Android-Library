package com.example.android.goeth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.example.android.goeth.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import io.naika.naikapay.toSummarisedAddress
import io.naika.naikapay.ui.*

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
                mainViewModel.loadContract(signedTxHash)
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


            val gifs = listOf("file:///android_asset/heads.gif", "file:///android_asset/tails.gif")

/*            Glide.with(this).asGif().load(gifs[random - 1]).listener(
                object : RequestListener<GifDrawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<GifDrawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: GifDrawable?,
                        model: Any?,
                        target: Target<GifDrawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.setLoopCount(1)
                        return false
                    }

                }
            ).into(binding.coinTossImageView)*/
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


    }

    private fun loadGifs(): List<String>? {
        val gifs: MutableList<String> = ArrayList()
        gifs.add("file:///android_asset/heads.gif")
        gifs.add("file:///android_asset/tails.gif")
        return gifs
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
    }

    override fun claimRewardClicked() {
        supportFragmentManager.popBackStack()

    }
}