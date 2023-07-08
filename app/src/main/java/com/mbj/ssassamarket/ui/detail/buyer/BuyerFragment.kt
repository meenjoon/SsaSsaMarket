package com.mbj.ssassamarket.ui.detail.buyer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentBuyerBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BuyerFragment : BaseFragment() {

    override val binding get() = _binding as FragmentBuyerBinding
    override val layoutId: Int get() = R.layout.fragment_buyer

    private val args: BuyerFragmentArgs by navArgs()
    private val viewModel: BuyerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setOtherUserId(args.product.id)

        viewModel.chatRoomId.observe(viewLifecycleOwner, EventObserver { chatRoomId ->
            viewModel.otherUserId.value?.let { navigateToChatDetailFragment(chatRoomId, it.peekContent()) }
        })

        binding.detailBuyerChatBt.setOnClickListener {
            viewModel.onChatButtonClicked(getString(R.string.test_seller_name), getString(R.string.test_seller_location))
        }

        binding.detailBackIv.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun navigateToChatDetailFragment(chatRoomId: String, otherId: String) {
        val action = BuyerFragmentDirections.actionBuyerFragmentToChatDetailFragment(
            chatRoomId = chatRoomId,
            otherId = otherId
        )
        findNavController().navigate(action)
    }
}
