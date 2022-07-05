package io.naika.naikapay.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.naika.naikapay.databinding.FragmentDialogWalletBinding
import org.ethereum.geth.Account
import org.ethereum.geth.BigInt
import org.ethereum.geth.Geth

class WalletDialogFragment: BottomSheetDialogFragment(),
    AccountsAdapter.OnAccountAdapterInteraction {

    private var walletDialogFragmentListener: WalletDialogFragmentInteraction? = null

    private lateinit var accountViewModel: AccountViewModel

    private var _binding: FragmentDialogWalletBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDialogWalletBinding.inflate(layoutInflater, container, false)
        val root: View = binding.root

        accountViewModel.createAccountIfNothingExist(requireContext())
        accountViewModel.getAccountList()
        binding.accountRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL , false)

        accountViewModel.accountList.observe(viewLifecycleOwner){
            val adapter = AccountsAdapter(it, this)
            binding.accountRecyclerView.adapter = adapter
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAccountClicked(account: Account) {
        val balance = accountViewModel.balanceHashMap[account.address.hex]
        val accInfo = AccountInfoModel(account = account, balance = balance)
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