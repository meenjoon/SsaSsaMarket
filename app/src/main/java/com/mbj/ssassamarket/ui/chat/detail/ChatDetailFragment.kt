package com.mbj.ssassamarket.ui.chat.detail

import com.mbj.ssassamarket.R
import com.mbj.ssassamarket.databinding.FragmentChatDetailBinding
import com.mbj.ssassamarket.ui.BaseFragment

class ChatDetailFragment : BaseFragment() {
    override val binding get() = _binding as FragmentChatDetailBinding
    override val layoutId: Int get() = R.layout.fragment_chat_detail
}
