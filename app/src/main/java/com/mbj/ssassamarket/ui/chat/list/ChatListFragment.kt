package com.mbj.ssassamarket.ui.chat.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.data.model.ChatRoomItem
import com.mbj.ssassamarket.databinding.FragmentChatListBinding
import com.mbj.ssassamarket.ui.BaseFragment
import com.mbj.ssassamarket.ui.common.ChatListClickListener
import com.mbj.ssassamarket.util.Colors
import com.mbj.ssassamarket.util.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatListFragment() : BaseFragment(), ChatListClickListener {
    @Inject
    lateinit var colors: Colors

    override val binding get() = _binding as FragmentChatListBinding
    override val layoutId: Int get() = R.layout.fragment_chat_list

    private lateinit var adapter: ChatListAdapter
    private val viewModel: ChatListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        observeChatRooms()

        viewModel.getChatRooms()
        viewModel.addChatRoomsValueEventListener()
    }

    private fun initRecyclerView() {
        adapter = ChatListAdapter(this, colors)
        binding.chatListRv.adapter = adapter
    }

    private fun observeChatRooms() {
        viewModel.chatRooms.observe(viewLifecycleOwner, EventObserver{ chatRooms ->
            adapter.submitList(chatRooms)
        })
    }

    override fun onChatRoomClicked(chatRoomItem: ChatRoomItem, otherImageColor: String) {
        val action = ChatListFragmentDirections.actionNavigationChatToChatDetailFragment(chatRoomItem = chatRoomItem, otherImageColor = otherImageColor)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeChatRoomsValueEventListener()
    }
}
