package com.mbj.ssassamarket.ui.chat

import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentChatBinding
import com.mbj.ssassamarket.ui.BaseFragment

class ChatFragment : BaseFragment() {
    override val binding get() = _binding as FragmentChatBinding
    override val layoutId: Int get() = R.layout.fragment_chat
}
