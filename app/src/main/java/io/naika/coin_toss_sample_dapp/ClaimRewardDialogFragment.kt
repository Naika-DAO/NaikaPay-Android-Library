package io.naika.coin_toss_sample_dapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.goeth.databinding.DialogClaimRewardBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ClaimRewardDialogFragment : BottomSheetDialogFragment() {

    private var claimRewardFragmentInteraction: ClaimRewardFragmentInteraction? = null


    private var _binding: DialogClaimRewardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogClaimRewardBinding.inflate(layoutInflater, container, false)
        val root: View = binding.root


        binding.claimRewardButton.setOnClickListener {
            claimRewardFragmentInteraction?.claimRewardClicked()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            claimRewardFragmentInteraction = activity as ClaimRewardFragmentInteraction
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement" + ClaimRewardFragmentInteraction::class.java.simpleName)
        }
    }

    override fun onDetach() {
        super.onDetach()
        claimRewardFragmentInteraction = null
    }


    interface ClaimRewardFragmentInteraction {
        fun claimRewardClicked()
    }

}