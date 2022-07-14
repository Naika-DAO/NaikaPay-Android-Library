package com.example.android.goeth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.android.goeth.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import io.naika.naikapay.ui.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var optionsMenu: Menu

    private val mainViewModel: MainViewModel by viewModels()

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val selectedAddress = data?.getStringExtra(ACCOUNT_ADDRESS_HASH)
            val balance = data?.getDoubleExtra(ACCOUNT_ADDRESS_BALANCE, 0.0)
            mainViewModel.isAccountConnected = true
            mainViewModel.selectedAddressHash = selectedAddress!!
            binding.addressTextView.text =
                String.format("%s: %s", "Your connected account", selectedAddress)
            binding.balanceTextView.text =
                String.format("%s: %.7f %s", "Your balance", balance, "ETH")
            binding.chanceTextView.text = String.format("%s: %s", "Your tickets", "0")
            val connectMenuItem = optionsMenu[0]
            connectMenuItem.title = getString(R.string.wallet_connected)
            val disconnectMenuItem = optionsMenu[1]
            disconnectMenuItem.isVisible = true
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mainViewModel.connectToNetwork()

        binding.playButton.setOnClickListener {

            val random = (1..2).shuffled().first()
            val gifs = listOf("file:///android_asset/heads.gif", "file:///android_asset/tails.gif")

            Glide.with(this).asGif().load(gifs[random - 1]).listener(
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
            ).into(binding.coinTossImageView)
        }

        binding.buyChancesButton.setOnClickListener {
            mainViewModel.createTransaction()

            //mainViewModel.createTransaction()
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


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_menu, menu)
        optionsMenu = menu
        val disconnectMenuItem = menu.findItem(R.id.menu_disconnect_wallet)
        disconnectMenuItem.isVisible = mainViewModel.isAccountConnected
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_connect_wallet -> {
                val  intent: Intent? = Intent()
                intent?.setClassName(this, "io.naika.naikapay.ui.WalletActivity")
                intent?.action = ACTION_GET_ACCOUNT
                resultLauncher.launch(intent)
            }
            R.id.menu_disconnect_wallet ->{
                disconnectAccount()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun disconnectAccount() {
        mainViewModel.isAccountConnected = false
        mainViewModel.selectedAddressHash = ""
        binding.addressTextView.text = ""
        val connectMenuItem = optionsMenu[0]
        connectMenuItem.title = getString(R.string.connect_wallet)
        val disconnectMenuItem = optionsMenu[1]
        disconnectMenuItem.isVisible = true
    }
}