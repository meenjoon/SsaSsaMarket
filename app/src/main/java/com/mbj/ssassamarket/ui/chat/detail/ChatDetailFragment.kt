package com.mbj.ssassamarket.ui.chat.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentChatDetailBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatDetailFragment : BaseFragment() {
    override val binding get() = _binding as FragmentChatDetailBinding
    override val layoutId: Int get() = R.layout.fragment_chat_detail

    private lateinit var adapter: ChatDetailAdapter
    private val args: ChatDetailFragmentArgs by navArgs()
    private val viewModel: ChatDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChatDetailAdapter()
        binding.chatDetailRv.adapter = adapter
        args.otherImageColor?.let { adapter.updateOtherImageColor(it) }
        setupViewModel()
        setupUI()
    }

    private fun setupViewModel() {
        viewModel.setChatRoomId(args.chatRoomItem?.chatRoomId ?: args.chatRoomId.toString())
        viewModel.setOtherUserId(args.chatRoomItem?.otherUserId ?: args.otherId.toString())

        lifecycleScope.launch {
            viewModel.addChatDetailEventListener()
        }

        viewModel.getMyUserItem()

        viewModel.getOtherUserId()?.let { viewModel.getOtherUserItem(it) }

        viewModel.chatItemList.observe(viewLifecycleOwner, EventObserver { chatItemList ->
            adapter.submitList(chatItemList)

            if (chatItemList.isNotEmpty()) {
                binding.chatDetailRv.post {
                    binding.chatDetailRv.scrollToPosition(chatItemList.size - 1)
                }
            }
        })

        viewModel.otherUserItem.observe(viewLifecycleOwner, EventObserver { otherUserItem ->
            adapter.updateOtherUserItem(otherUserItem)
            binding.chatDetailNicknameAbTv.text = otherUserItem.userName
        })
    }

    private fun setupUI() {
        binding.chatDetailBackIv.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.chatDetailSendIv.setOnClickListener {
            val message = binding.chatDetailTiev.text.toString()

            if (message.isEmpty()) {
                showToast(R.string.empty_message_error)
                return@setOnClickListener
            }
            viewModel.sendMessage(message)

            binding.chatDetailTiev.text?.clear()
        }
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeChatDetailEventListener()
    }
}
