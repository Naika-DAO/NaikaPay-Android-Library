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
import com.example.android.goeth.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import io.naika.naikapay.ui.ACCOUNT_ADDRESS_BALANCE
import io.naika.naikapay.ui.ACCOUNT_ADDRESS_HASH

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
            binding.addressTextView.text = String.format("%s: %s", "Your connected account" , selectedAddress)
            binding.balanceTextView.text = String.format("%s: %.7f %s", "Your balance", balance, "ETH")
            binding.chanceTextView.text = String.format("%s: %s", "Your tickets" , "0")
            val connectMenuItem = optionsMenu[0]
            connectMenuItem.title = getString(R.string.wallet_connected)
            val disconnectMenuItem = optionsMenu[1]
            disconnectMenuItem.isVisible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
        binding.addressTextView.text = ""
        val connectMenuItem = optionsMenu[0]
        connectMenuItem.title = getString(R.string.connect_wallet)
        val disconnectMenuItem = optionsMenu[1]
        disconnectMenuItem.isVisible = true
    }
}