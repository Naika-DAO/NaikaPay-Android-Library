package io.naika.naikapay.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.naika.naikapay.R

class WalletChooserDialogFragment : DialogFragment() {

    private var walletChooserDialogFragment: WalletChooserDialogFragmentInteraction? = null
    private var dismissedByUserAction = false


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val builder = MaterialAlertDialogBuilder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_dialog_chooser_wallet, null)
            builder.setView(view)

            view.findViewById<Button>(R.id.chooser_dialog_naika_pay_button).setOnClickListener {
                walletChooserDialogFragment?.onWalletSelected(WalletType.NAIKA_PAY)
                dismissedByUserAction = true
                dismiss()
            }

            view.findViewById<Button>(R.id.chooser_dialog_metamask_button).setOnClickListener {
                walletChooserDialogFragment?.onWalletSelected(WalletType.METAMASK)
                dismissedByUserAction = true
                dismiss()
            }


            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!dismissedByUserAction) {
            walletChooserDialogFragment?.onDismissCalled()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            walletChooserDialogFragment = activity as WalletChooserDialogFragmentInteraction
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement" + WalletChooserDialogFragmentInteraction::class.java.simpleName)
        }
    }

    override fun onDetach() {
        super.onDetach()
        walletChooserDialogFragment = null
    }

    interface WalletChooserDialogFragmentInteraction {
        fun onWalletSelected(walletType: WalletType)
        fun onDismissCalled()
    }

    enum class WalletType {
        NAIKA_PAY,
        METAMASK
    }

}