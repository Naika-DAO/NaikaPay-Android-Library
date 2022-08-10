package io.naika.naikapay.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.naika.naikapay.calculateGasFeeInETH
import io.naika.naikapay.convertBigIntBalanceToDouble
import io.naika.naikapay.databinding.FragmentDialogSignBinding
import io.naika.naikapay.getMethodName
import io.naika.naikapay.toSummarisedAddress
import org.ethereum.geth.Transaction
import org.komputing.khex.extensions.toHexString

const val SIGN_DIALOG_TX = "tx"
const val SIGN_DIALOG_ADDRESS = "address"
const val SIGN_DIALOG_ADDRESS_BALANCE = "address_balance"


class SignDialogFragment : BottomSheetDialogFragment() {

    private var signDialogFragmentListener: SignDialogFragmentInteraction? = null

    private lateinit var accountViewModel: AccountViewModel
    private lateinit var txBytes: ByteArray
    private lateinit var signerAddressHash: String
    private var signerBalance: Double = 0.0

    private var _binding: FragmentDialogSignBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountViewModel = ViewModelProvider(activity!!)[AccountViewModel::class.java]
        signerAddressHash = arguments?.getString(SIGN_DIALOG_ADDRESS) ?: ""
        signerBalance = arguments?.getDouble(SIGN_DIALOG_ADDRESS_BALANCE) ?: 0.0
        txBytes = arguments?.getByteArray(SIGN_DIALOG_TX)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDialogSignBinding.inflate(layoutInflater, container, false)
        val root: View = binding.root


        binding.addressHash.text = toSummarisedAddress(signerAddressHash)
        binding.addressBalance.text = String.format("%.5f", signerBalance)

        val transaction = Transaction(txBytes)

        val methodName = getMethodName(
            "[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"inputs\":[],\"name\":\"buyChance\",\"outputs\":[],\"stateMutability\":\"payable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"claim\",\"outputs\":[],\"stateMutability\":\"payable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"deposit\",\"outputs\":[],\"stateMutability\":\"payable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getBalance\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"owner\",\"outputs\":[{\"internalType\":\"address payable\",\"name\":\"\",\"type\":\"address\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"withdraw\",\"outputs\":[],\"stateMutability\":\"payable\",\"type\":\"function\"}]",
            transaction.data.toHexString()
        )

        Log.d("Babak", methodName)


        val estimateGas = accountViewModel.getEstimateGasFee(transaction)
        val estimatedGasInETH = calculateGasFeeInETH(accountViewModel.getGasPrice(), estimateGas)
        Log.d("GAS", estimatedGasInETH.toString())
        binding.estimateGasETH.text = String.format("%.6f ETH", estimatedGasInETH)

        binding.toAddressHash.text = toSummarisedAddress(transaction.to.hex)

        val value = convertBigIntBalanceToDouble(transaction.value)
        binding.valueTextView.text = String.format("%.6f ETH", value)

        binding.totalEstimateETH.text = String.format("%.6f ETH", estimatedGasInETH + value)

        binding.rejectTransaction.setOnClickListener {
            dismiss()
        }
        binding.signTransaction.setOnClickListener {
            if (binding.passwordEditText.text.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter a valid password.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            accountViewModel.signTx(
                transaction,
                signerAddressHash,
                binding.passwordEditText.text.toString()
            )


        }
        accountViewModel.error.observe(this) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
        }

        accountViewModel.signedTx.observe(this) { tx ->

            val rlp = tx.encodeRLP()
            signDialogFragmentListener?.onConfirmClicked(rlp)


        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        signDialogFragmentListener?.onDismissCalled()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            signDialogFragmentListener = activity as SignDialogFragmentInteraction
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement" + SignDialogFragmentInteraction::class.java.simpleName)
        }
    }

    override fun onDetach() {
        super.onDetach()
        signDialogFragmentListener = null
    }

    interface SignDialogFragmentInteraction {
        fun onDismissCalled()
        fun onConfirmClicked(signedTX: ByteArray)
    }

}