package com.mbj.ssassamarket.ui.common

import com.mbj.ssassamarket.data.model.ChatRoomItem

interface ChatListClickListener {
    fun onChatRoomClicked(chatRoomItem: ChatRoomItem)
}
