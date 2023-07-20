package com.mbj.ssassamarket.ui.chat.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mbj.ssassamarket.data.model.ChatRoomItem
import com.mbj.ssassamarket.databinding.RecyclerviewItemChatRoomBinding
import com.mbj.ssassamarket.ui.common.ChatListClickListener
import com.mbj.ssassamarket.util.Colors
import javax.inject.Inject

class ChatListAdapter@Inject constructor(
    private val chatListClickListener: ChatListClickListener,
    private val colors: Colors
) : ListAdapter<ChatRoomItem, ChatListAdapter.ChatListViewHolder>(ChatListDiffCallback()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        return ChatListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(currentList[position], chatListClickListener, colors)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }


    class ChatListViewHolder(private val binding: RecyclerviewItemChatRoomBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatRoomItem: ChatRoomItem, chatListClickListener: ChatListClickListener, colors: Colors) {
            binding.chatRoomItem = chatRoomItem
            binding.chatListClickListener = chatListClickListener
            binding.randomColor = colors.randomColor
        }

        companion object {
            fun from(parent: ViewGroup): ChatListViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ChatListViewHolder(
                    RecyclerviewItemChatRoomBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }
        }
    }

    private class ChatListDiffCallback : DiffUtil.ItemCallback<ChatRoomItem>() {
        override fun areItemsTheSame(oldItem: ChatRoomItem, newItem: ChatRoomItem): Boolean {
            return oldItem.chatRoomId == newItem.chatRoomId
        }

        override fun areContentsTheSame(oldItem: ChatRoomItem, newItem: ChatRoomItem): Boolean {
            return oldItem == newItem
        }
    }
}
