package com.mbj.ssassamarket.ui.chat.list

import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentChatListBinding
import com.mbj.ssassamarket.ui.BaseFragment

class ChatListFragment : BaseFragment() {
    override val binding get() = _binding as FragmentChatListBinding
    override val layoutId: Int get() = R.layout.fragment_chat_list
}
