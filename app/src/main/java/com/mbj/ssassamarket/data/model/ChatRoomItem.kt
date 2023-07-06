package com.mbj.ssassamarket.data.model

import java.io.Serializable

data class ChatRoomItem(
    val chatRoomId: String? = null,
    val otherUserName: String? = null,
    val lastMessage: String? = null,
    val otherUserId: String? = null,
    val otherLocation: String? = null,
    val lastSentTime: String? = null
) : Serializable
