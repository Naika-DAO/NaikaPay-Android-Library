package com.example.android.goeth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.android.goeth.databinding.FragmentTransactionResponseBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


const val TX_HASH = "tx_hash"

class TransactionResponseDialog : BottomSheetDialogFragment() {

    private var _binding: FragmentTransactionResponseBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransactionResponseBinding.inflate(layoutInflater, container, false)
        val root: View = binding.root

        val txHash = arguments?.getString(TX_HASH)
        binding.titleTextView.text = String.format("%s: %s", "TX Hash", txHash)

        binding.subTitleTextView.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://goerli.etherscan.io/tx/$txHash"))
            startActivity(browserIntent)
        }

        binding.copyImageView.setOnClickListener {
            val clipboard: ClipboardManager? =
                activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("tx_hash", txHash)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "TX Hash copied to clip board", Toast.LENGTH_LONG)
                .show()
        }

        binding.okButton.setOnClickListener {
            dismiss()
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}