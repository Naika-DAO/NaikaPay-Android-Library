package com.example.android.goeth

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.android.goeth.databinding.DialogBuyChancesBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BuyChancesDialog : BottomSheetDialogFragment() {

    private var buyChancesFragmentInteraction: BuyChancesFragmentInteraction? = null

    private var _binding: DialogBuyChancesBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogBuyChancesBinding.inflate(layoutInflater, container, false)
        val root: View = binding.root

        binding.dialogBuyChanceButton.setOnClickListener {
            if (binding.numberOfChancesEditText.text.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please specify the number of chances",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (binding.numberOfChancesEditText.text?.toString()?.toInt() == 0) {
                Toast.makeText(requireContext(), "You can not buy 0 chances", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            buyChancesFragmentInteraction?.onBuyChancesClicked(
                binding.numberOfChancesEditText.text?.toString()?.toInt()!!
            )
        }

        binding.numberOfChancesEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!s.isNullOrBlank()) {
                    val number = s.toString().toInt()
                    val totalPrice = number * CHANCE_PRICE
                    binding.amountTextView.text =
                        String.format("%s: %f %s", "Amount", totalPrice, "ETH")
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            buyChancesFragmentInteraction = activity as BuyChancesFragmentInteraction
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement" + BuyChancesFragmentInteraction::class.java.simpleName)
        }
    }

    override fun onDetach() {
        super.onDetach()
        buyChancesFragmentInteraction = null
    }

    interface BuyChancesFragmentInteraction {
        fun onBuyChancesClicked(numberOfChances: Int)
    }
}