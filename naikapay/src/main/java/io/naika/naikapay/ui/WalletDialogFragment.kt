package io.naika.naikapay.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.naika.naikapay.databinding.FragmentDialogWalletBinding


class WalletDialogFragment: BottomSheetDialogFragment(),
    AccountsAdapter.OnAccountAdapterInteraction {

    private var walletDialogFragmentListener: WalletDialogFragmentInteraction? = null

    private lateinit var accountViewModel: AccountViewModel

    private var _binding: FragmentDialogWalletBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountViewModel = ViewModelProvider(activity!!)[AccountViewModel::class.java]

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDialogWalletBinding.inflate(layoutInflater, container, false)
        val root: View = binding.root


        accountViewModel.getAccountList()
        binding.accountRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val adapter = AccountsAdapter(this)
        binding.accountRecyclerView.adapter = adapter


        accountViewModel.accountList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.emptyViewButton.visibility = View.VISIBLE
                binding.emptyViewText.visibility = View.VISIBLE
            } else {
                binding.emptyViewButton.visibility = View.GONE
                binding.emptyViewText.visibility = View.INVISIBLE
                adapter.addData(it)
            }

        }

        accountViewModel.accountListChanged.observe(viewLifecycleOwner) {

            adapter.setData(it)

        }

        binding.emptyViewButton.setOnClickListener {

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose a password")
            val input = EditText(requireContext())
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            lp.setMargins(8, 8, 8, 8)
            input.layoutParams = lp
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            builder.setView(input)

            builder.setPositiveButton("Create") { dialog, n ->
                if (!input.text.isNullOrBlank()) {
                    accountViewModel.createAccount(requireContext(), input.text.toString())
                }
            }

            builder.setNegativeButton("Cancel") { dialog, n ->
                dialog.dismiss()
            }
            builder.show()


        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAccountClicked(account: AccountUIModel) {

        if (account.balance == null) {
            return
        }

        val accInfo = AccountInfoModel(account = account.account, balance = account.balance)
        walletDialogFragmentListener?.onAccountSelected(accInfo)
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        walletDialogFragmentListener?.onDismissCalled()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            walletDialogFragmentListener = activity as WalletDialogFragmentInteraction
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement" + WalletDialogFragmentInteraction::class.java.simpleName)
        }
    }

    override fun onDetach() {
        super.onDetach()
        walletDialogFragmentListener = null
    }

    interface WalletDialogFragmentInteraction{
        fun onAccountSelected(accountInfoModel: AccountInfoModel)
        fun onDismissCalled()
    }
}