package com.example.android.goeth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.android.goeth.databinding.FragmentPlayBinding

class PlayFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()

    private var playFragmentInteraction: PlayFragmentInteraction? = null


    private var _binding: FragmentPlayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPlayBinding.inflate(layoutInflater, container, false)
        val root: View = binding.root

        val imageList = listOf(R.drawable.ic_head, R.drawable.ic_tails)

        val imageAdapter = ImageViewPagerAdapter(imageList)
        binding.playViewPager.adapter = imageAdapter
        binding.playViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val currentPageIndex = 0
        binding.playViewPager.currentItem = currentPageIndex

        binding.playViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when (position) {
                        0 -> {
                            binding.playCoinTitle.text = getString(R.string.head)
                        }
                        1 -> {
                            binding.playCoinTitle.text = getString(R.string.tails)
                        }
                    }

                }
            }
        )

        binding.playRightArrow.setOnClickListener {
            var cItem = binding.playViewPager.currentItem
            cItem = if (cItem == 0) {
                1
            } else {
                0
            }
            binding.playViewPager.currentItem = cItem
        }

        binding.playLeftArrow.setOnClickListener {
            var cItem = binding.playViewPager.currentItem
            cItem = if (cItem == 0) {
                1
            } else {
                0
            }
            binding.playViewPager.currentItem = cItem
        }



        binding.tossCoinButton.setOnClickListener {

            if (mainViewModel.coinTossRounds[mainViewModel.currentRound] == -1) {
                enterPlayMode(mainViewModel.currentRound)
            } else {
                if (mainViewModel.currentRound < 2) {
                    mainViewModel.currentRound = mainViewModel.currentRound + 1
                    setUpFirstStep()
                } else {
                    //finish the game
                    if (
                        mainViewModel.coinTossRounds[0] == 1 &&
                        mainViewModel.coinTossRounds[1] == 1 &&
                        mainViewModel.coinTossRounds[2] == 1
                    ) {
                        //won the game
                        val claimRewardDialogFragment = ClaimRewardDialogFragment()
                        claimRewardDialogFragment.isCancelable = false
                        claimRewardDialogFragment.show(childFragmentManager, "claim")
                        resetGameData()

                    } else {
                        resetGameData()
                        playFragmentInteraction?.finishGameWithLost()
                    }
                }
            }

        }

        return root
    }

    private fun resetGameData() {
        mainViewModel.chanceLeft = 0
        mainViewModel.currentRound = 0
        mainViewModel.predictedCoinState = 0
        mainViewModel.coinTossRounds[0] = -1
        mainViewModel.coinTossRounds[1] = -1
        mainViewModel.coinTossRounds[2] = -1
    }

    private fun setUpFirstStep() {
        binding.playViewPager.isUserInputEnabled = true
        binding.playViewPager.currentItem = 0
        binding.playRightArrow.visibility = View.VISIBLE
        binding.playLeftArrow.visibility = View.VISIBLE
        binding.textView5.visibility = View.VISIBLE
        binding.playResultTitleTextView.visibility = View.GONE
        binding.tossCoinButton.text = getString(R.string.toss_the_coin)

        when (mainViewModel.currentRound) {
            1 -> {
                binding.secondStep.setImageResource(R.drawable.ic_current_step_background)
                binding.secondStepTextView.visibility = View.VISIBLE
            }
            2 -> {
                binding.thirdStep.setImageResource(R.drawable.ic_current_step_background)
                binding.thirdStepTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun enterPlayMode(currentRound: Int) {
        val random = (1..2).shuffled().first()
        binding.playViewPager.isUserInputEnabled = false
        mainViewModel.predictedCoinState = binding.playViewPager.currentItem
        binding.playViewPager.currentItem = random - 1
        binding.playRightArrow.visibility = View.INVISIBLE
        binding.playLeftArrow.visibility = View.INVISIBLE
        binding.textView5.visibility = View.INVISIBLE
        binding.playResultTitleTextView.visibility = View.VISIBLE
        if (random - 1 == mainViewModel.predictedCoinState) {
            //you won
            mainViewModel.coinTossRounds[currentRound] = 1
            binding.playResultTitleTextView.text = getString(R.string.you_won)
            binding.playResultTitleTextView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green
                )
            )
            when (currentRound) {
                0 -> {
                    binding.firstStep.setImageResource(R.drawable.ic_passed_step_background)
                    binding.firstStepTextView.visibility = View.GONE
                    binding.firstStepImage.visibility = View.VISIBLE
                    binding.firstStepImage.setImageResource(R.drawable.ic_tik)
                }
                1 -> {
                    binding.secondStep.setImageResource(R.drawable.ic_passed_step_background)
                    binding.secondStepTextView.visibility = View.GONE
                    binding.secondStepImage.visibility = View.VISIBLE
                    binding.secondStepImage.setImageResource(R.drawable.ic_tik)
                }
                2 -> {
                    binding.thirdStep.setImageResource(R.drawable.ic_passed_step_background)
                    binding.thirdStepTextView.visibility = View.GONE
                    binding.thirdStepImage.visibility = View.VISIBLE
                    binding.thirdStepImage.setImageResource(R.drawable.ic_tik)
                }
            }
        } else {
            //you lost
            mainViewModel.coinTossRounds[currentRound] = 0
            binding.playResultTitleTextView.text = getString(R.string.you_lost)
            binding.playResultTitleTextView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            )
            when (currentRound) {
                0 -> {
                    binding.firstStep.setImageResource(R.drawable.ic_failed_step_background)
                    binding.firstStepTextView.visibility = View.GONE
                    binding.firstStepImage.visibility = View.VISIBLE
                    binding.firstStepImage.setImageResource(R.drawable.ic_cross)
                }
                1 -> {
                    binding.secondStep.setImageResource(R.drawable.ic_failed_step_background)
                    binding.secondStepTextView.visibility = View.GONE
                    binding.secondStepImage.visibility = View.VISIBLE
                    binding.secondStepImage.setImageResource(R.drawable.ic_cross)
                }
                2 -> {
                    binding.thirdStep.setImageResource(R.drawable.ic_failed_step_background)
                    binding.thirdStepTextView.visibility = View.GONE
                    binding.thirdStepImage.visibility = View.VISIBLE
                    binding.thirdStepImage.setImageResource(R.drawable.ic_cross)
                }
            }
        }

        when (currentRound) {
            0 -> {
                binding.tossCoinButton.text = getString(R.string.play_second_round)

            }
            1 -> {
                binding.tossCoinButton.text = getString(R.string.play_third_round)

            }
            2 -> {
                binding.tossCoinButton.text = "Finish"

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        resetGameData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            playFragmentInteraction = activity as PlayFragmentInteraction
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement" + PlayFragmentInteraction::class.java.simpleName)
        }
    }

    override fun onDetach() {
        super.onDetach()
        playFragmentInteraction = null
    }

    interface PlayFragmentInteraction {
        fun finishGameWithLost()
    }


}