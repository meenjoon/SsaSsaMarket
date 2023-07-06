package com.mbj.ssassamarket.ui.chat.detail

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mbj.ssassamarket.data.model.ChatItem
import com.mbj.ssassamarket.data.model.User
import com.mbj.ssassamarket.databinding.RecyclerviewItemMeChatBinding
import com.mbj.ssassamarket.databinding.RecyclerviewItemOtehrChatBinding
import com.mbj.ssassamarket.util.TextFormat.getInitialLetter

class ChatDetailAdapter() : ListAdapter<ChatItem, RecyclerView.ViewHolder>(ChatListDiffCallback()) {

    private var otherUser: User? = null
    private var otherImageColor: String? = null

    fun updateOtherUserItem(user: User?) {
        otherUser = user
        notifyDataSetChanged()
    }

    fun updateOtherImageColor(color: String) {
        otherImageColor = color
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ME -> {
                val binding = RecyclerviewItemMeChatBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MeViewHolder(binding)
            }
            VIEW_TYPE_OTHER -> {
                val binding = RecyclerviewItemOtehrChatBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                OtherViewHolder(binding, otherUser)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatItem = getItem(position)
        when (holder) {
            is MeViewHolder -> holder.bind(chatItem)
            is OtherViewHolder -> holder.bind(chatItem, otherImageColor)
            else -> throw IllegalArgumentException("Invalid view holder")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val chatItem = getItem(position)
        return when {
            chatItem.userId == otherUser?.userId -> VIEW_TYPE_OTHER
            else -> VIEW_TYPE_ME
        }
    }

    class MeViewHolder(private val binding: RecyclerviewItemMeChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatItem: ChatItem) {
            binding.chatMeMessageTv.text = chatItem.message
        }
    }

    class OtherViewHolder(private val binding: RecyclerviewItemOtehrChatBinding, private val otherUser: User?) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatItem: ChatItem, otherImageColor: String?) {
            if (otherImageColor != null) {
                binding.chatOtherUserIv.setColorFilter(Color.parseColor(otherImageColor), PorterDuff.Mode.SRC_IN)
            }
            binding.chatOtherNicknameTv.text = otherUser?.userName
            binding.chatOtherMessageTv.text = chatItem.message
            binding.chatOtherUserFirstNicknameTv.text = otherUser?.userName?.let { getInitialLetter(it) }
        }
    }

    companion object {
        private const val VIEW_TYPE_ME = 0
        private const val VIEW_TYPE_OTHER = 1
    }

    private class ChatListDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
        override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem == newItem
        }
    }
}
